package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.RollableScalableResource;
import io.fabric8.zjsonpatch.internal.guava.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.PluginVariable;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@Slf4j
@Service
public class EntandoPluginService extends EntandoKubernetesResourceCollector<EntandoPlugin> {

    public static final boolean CREATE = false;
    public static final boolean CREATE_OR_REPLACE = true;

    public static final String ENTANDO_TIMESTAMP_NAME = "ENTANDO_TIMESTAMP";
    public static final String ENTANDO_PLUGINS_VARIABLES_CONFIG_MAP = "entando-plugins-variables";

    public EntandoPluginService(KubernetesUtils kubernetesUtils,
            ObservedNamespaces observedNamespaces) {
        super(kubernetesUtils, observedNamespaces);
    }

    @Override
    protected List<EntandoPlugin> getInAnyNamespace() {
        return getPluginOperations().inAnyNamespace().list().getItems();
    }

    @Override
    protected List<EntandoPlugin> getInNamespaceWithoutChecking(String namespace) {
        return getPluginOperations().inNamespace(namespace).list().getItems();
    }

    public List<PluginVariable> resolvePluginVariables(List<String> pluginVariables, String namespace) {
        if (CollectionUtils.isEmpty(pluginVariables)) {
            return Collections.emptyList();
        }

        final Map<String, String> data = getConfigMap(namespace, ENTANDO_PLUGINS_VARIABLES_CONFIG_MAP)
                .map(ConfigMap::getData)
                .orElseThrow(() -> NotFoundExceptionFactory.configMap(ENTANDO_PLUGINS_VARIABLES_CONFIG_MAP, namespace));

        return pluginVariables.stream()
                .map(var -> new PluginVariable(var, data.get(var)))
                .collect(Collectors.toList());
    }

    public void deletePlugin(String pluginId) {
        log.info("Delete plugin {} from observed namespaces", pluginId);
        Optional<EntandoPlugin> entandoPlugin = findByName(pluginId);
        entandoPlugin.ifPresent(this::deletePlugin);
    }

    public void deletePlugin(EntandoPlugin plugin) {
        log.info("Delete plugin {} from observed namespaces", plugin.getMetadata().getName());
        getPluginOperations().inAnyNamespace().delete(plugin);
    }

    public void deletePluginInNamespace(String pluginId, String namespace) {
        log.info("Delete plugin {} from namespace {}", pluginId, namespace);
        EntandoPlugin pluginToRemove = new EntandoPlugin();
        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName(pluginId).withNamespace(namespace).build();
        pluginToRemove.setMetadata(pluginMeta);
        getPluginOperations().inAnyNamespace().delete(pluginToRemove);
    }

    public EntandoPlugin deploy(EntandoPlugin plugin) {
        return deploy(plugin, CREATE);
    }

    public EntandoPlugin deploy(EntandoPlugin plugin, boolean createOrReplace) {
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        EntandoPlugin cleanPlugin = pluginCleanUp(plugin);
        cleanPlugin.getMetadata().setNamespace(namespace);
        addEntandoTimestampEnvVar(cleanPlugin);

        log.info("Deploying {} plugin {} in namespace {}",
                (createOrReplace) ? "(createOrReplace)" : "(create)",
                plugin.getMetadata().getName(),
                namespace);

        if (createOrReplace) {
            return getPluginOperations().inNamespace(namespace).createOrReplace(cleanPlugin);
        } else {
            return getPluginOperations().inNamespace(namespace).create(cleanPlugin);
        }
    }

    private void addEntandoTimestampEnvVar(EntandoPlugin plugin) {
        EnvVar envVar = new EnvVarBuilder()
                .withName(ENTANDO_TIMESTAMP_NAME)
                .withValue(System.currentTimeMillis() + "")
                .build();
        plugin.getSpec().getEnvironmentVariables().add(envVar);
    }

    private EntandoPlugin pluginCleanUp(EntandoPlugin plugin) {
        if (Strings.isNullOrEmpty(plugin.getMetadata().getName())) {
            throw Problem.builder().withStatus(Status.BAD_REQUEST).withDetail("Plugin name is null or empty!").build();
        }
        if (Strings.isNullOrEmpty(plugin.getMetadata().getNamespace())) {
            plugin.getMetadata().setNamespace(observedNamespaces.getCurrentNamespace());
        }
        EntandoPlugin newPlugin = new EntandoPluginBuilder()
                .withNewMetadata()
                .withName(plugin.getMetadata().getName())
                .withNamespace(plugin.getMetadata().getNamespace())
                .endMetadata()
                .withSpec(plugin.getSpec())
                .build();
        newPlugin.setStatus(null);
        return newPlugin;
    }

    public void scaleDownPlugin(EntandoPlugin entandoPlugin) {
        RollableScalableResource<Deployment> pluginDeployment = kubernetesUtils.getCurrentKubernetesClient()
                .apps()
                .deployments()
                .inNamespace(entandoPlugin.getMetadata().getNamespace())
                .withName(entandoPlugin.getMetadata().getName() + "-deployment");

        if (pluginDeployment != null) {
            pluginDeployment.scale(0);
        }
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoPlugin, KubernetesResourceList<EntandoPlugin>, Resource<EntandoPlugin>> getPluginOperations() {
        //CHECKSTYLE:ON
        return kubernetesUtils.getCurrentKubernetesClient().customResources(EntandoPlugin.class);
    }

    private Optional<ConfigMap> getConfigMap(String namespace, String configMapName) {
        MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> configMapMixedOperation = kubernetesUtils.getCurrentKubernetesClient()
                .configMaps();
        if (! Strings.isNullOrEmpty(namespace)) {
            configMapMixedOperation = (MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>>) configMapMixedOperation.inNamespace(namespace);
        } else {
            configMapMixedOperation = (MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>>) configMapMixedOperation.inAnyNamespace();
        }
        return Optional.ofNullable(configMapMixedOperation.withName(configMapName).get());
    }
}
