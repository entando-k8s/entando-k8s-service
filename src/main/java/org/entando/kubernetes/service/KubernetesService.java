package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListMultiDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.zjsonpatch.internal.guava.Strings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.*;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.EntandoCustomResourceStatus;
import org.entando.kubernetes.model.EntandoDeploymentPhase;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class KubernetesService {

    public static final String ENTANDOPLUGIN_CRD_NAME = "entandoplugins.entando.org";

    private final @NonNull KubernetesClient client;

    public KubernetesService(@Autowired final KubernetesClient client) {
        this.client = client;
    }

    public void deletePlugin(String pluginId) {
        Optional<EntandoPlugin> entandoPlugin = findPluginById(pluginId);
        entandoPlugin.ifPresent(pl -> deletePluginInNamespace(pluginId, pl.getMetadata().getNamespace()));
    }

    public void deletePluginInNamespace(String pluginId, String namespace) {
        EntandoPlugin pluginToRemove = new EntandoPlugin();
        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName(pluginId).withNamespace(namespace).build();
        pluginToRemove.setMetadata(pluginMeta);
        getEntandoPluginOperations().inAnyNamespace().delete(pluginToRemove);
    }

    public List<EntandoPlugin> getAllPlugins() {
        return getEntandoPluginOperations().inAnyNamespace().list().getItems();
    }

    public List<EntandoPlugin> getAllPluginsInNamespace(String namespace) {
        return getEntandoPluginOperations().inNamespace(namespace).list().getItems();
    }

    public void deploy(EntandoPluginDeploymentRequest request) {
        final EntandoPlugin plugin = new EntandoPlugin();
        final ObjectMeta objectMeta = new ObjectMeta();

        objectMeta.setName(request.getId());

        EntandoPluginSpec.EntandoPluginSpecBuilder specBuilder = new EntandoPluginSpec.EntandoPluginSpecBuilder();
        specBuilder.withDbms(DbmsImageVendor.forValue(request.getDbms()));
        specBuilder.withImage(request.getImage());
        specBuilder.withIngressPath(request.getIngressPath());
        specBuilder.withHealthCheckPath(request.getHealthCheckPath());
        specBuilder.withEntandoApp(request.getNamespace(),request.getEntandoAppName());
        specBuilder.withReplicas(1);

        request.getRoles().forEach(r -> specBuilder.withRole(r.getCode(), r.getName()));
        request.getPermissions().forEach(p -> specBuilder.withPermission(p.getClientId(), p.getRole()));

        plugin.setMetadata(objectMeta);
        plugin.setSpec(specBuilder.build());
        plugin.setApiVersion("entando.org/v1alpha1");

        getEntandoPluginOperations().inNamespace(request.getNamespace()).create();
    }

    public EntandoPlugin deploy(EntandoPlugin plugin) {
        if (Strings.isNullOrEmpty(plugin.getMetadata().getNamespace())) {
            plugin.getMetadata().setNamespace(client.getNamespace());
        }
        return getEntandoPluginOperations().inNamespace(plugin.getMetadata().getNamespace()).create(plugin);
    }


    private LocalDateTime localDateTime(final PodCondition condition) {
        return LocalDateTime.parse(condition.getLastTransitionTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private LocalDateTime localDateTime(final DeploymentCondition condition) {
        return LocalDateTime.parse(condition.getLastTransitionTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private <T> List<T> sort(final List<T> list, final Comparator<T> comparator) {
        final List<T> sortedList = new ArrayList<>(list);
        sortedList.sort(comparator);
        return sortedList;
    }

    public Optional<EntandoPlugin> findPluginById(String pluginId) {
        return getEntandoPluginOperations().inAnyNamespace().list().getItems().stream().filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    public Optional<EntandoPlugin> findPluginByIdAndNamespace(String pluginId, String namespace) {
        return getEntandoPluginOperations().inNamespace(namespace).list().getItems().stream().filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    private MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> getEntandoPluginOperations() {
        CustomResourceDefinition entandoPluginCrd = client.customResourceDefinitions()
                .withName(ENTANDOPLUGIN_CRD_NAME).get();
        return client.customResources(entandoPluginCrd, EntandoPlugin.class, EntandoPluginList.class, DoneableEntandoPlugin.class);
    }

}
