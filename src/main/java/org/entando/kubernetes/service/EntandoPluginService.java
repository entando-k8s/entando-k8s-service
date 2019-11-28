package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.zjsonpatch.internal.guava.Strings;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EntandoPluginService {

    @NonNull
    private final KubernetesClient client;

    public EntandoPluginService(@Autowired final KubernetesClient client) {
        this.client = client;
    }

    public void deletePlugin(String pluginId) {
        log.info("Delete plugin {} from any namespace", pluginId);
        Optional<EntandoPlugin> entandoPlugin = findPluginById(pluginId);
        entandoPlugin.ifPresent(pl -> deletePluginInNamespace(pluginId, pl.getMetadata().getNamespace()));
    }

    public void deletePluginInNamespace(String pluginId, String namespace) {
        log.info("Delete plugin {} from namespace {}", pluginId, namespace);
        EntandoPlugin pluginToRemove = new EntandoPlugin();
        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName(pluginId).withNamespace(namespace).build();
        pluginToRemove.setMetadata(pluginMeta);
        getPluginOperations().inAnyNamespace().delete(pluginToRemove);
    }

    public List<EntandoPlugin> getAllPlugins() {
        return getPluginOperations().inAnyNamespace().list().getItems();
    }

    public List<EntandoPlugin> getAllPluginsInNamespace(String namespace) {
        return getPluginOperations().inNamespace(namespace).list().getItems();
    }

    public EntandoPlugin deploy(EntandoPlugin plugin) {
        log.info("Deploying plugin {} in namespace {}", plugin.getMetadata().getName(), plugin.getMetadata().getNamespace());
        EntandoPlugin cleanPlugin = pluginCleanUp(plugin);
        return getPluginOperations().inNamespace(cleanPlugin.getMetadata().getNamespace()).create(cleanPlugin);
    }

    private EntandoPlugin pluginCleanUp(EntandoPlugin plugin) {
        //TODO verify the plugin has a name
        //assert !Strings.isNullOrEmpty(plugin.getMetadata().getName());
        if (Strings.isNullOrEmpty(plugin.getMetadata().getNamespace())) {
            plugin.getMetadata().setNamespace(client.getNamespace());
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


    public Optional<EntandoPlugin> findPluginById(String pluginId) {
        return getPluginOperations().inAnyNamespace().list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    public Optional<EntandoPlugin> findPluginByIdAndNamespace(String pluginId, String namespace) {
        return getPluginOperations().inNamespace(namespace).list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> getPluginOperations() {
        //CHECKSTYLE:ON
        CustomResourceDefinition entandoPluginCrd = client.customResourceDefinitions()
                .withName(EntandoPlugin.CRD_NAME).get();
        return client.customResources(entandoPluginCrd, EntandoPlugin.class, EntandoPluginList.class,
                DoneableEntandoPlugin.class);
    }

}
