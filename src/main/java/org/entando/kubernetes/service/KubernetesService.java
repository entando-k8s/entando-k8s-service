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
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public EntandoPlugin deploy(EntandoPlugin plugin) {
        if (Strings.isNullOrEmpty(plugin.getMetadata().getNamespace())) {
            plugin.getMetadata().setNamespace(client.getNamespace());
        }
        plugin.setStatus(null);
        return getEntandoPluginOperations().inNamespace(plugin.getMetadata().getNamespace()).create(plugin);
    }


    public Optional<EntandoPlugin> findPluginById(String pluginId) {
        return getEntandoPluginOperations().inAnyNamespace().list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    public Optional<EntandoPlugin> findPluginByIdAndNamespace(String pluginId, String namespace) {
        return getEntandoPluginOperations().inNamespace(namespace).list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> getEntandoPluginOperations() {
    //CHECKSTYLE:ON
        CustomResourceDefinition entandoPluginCrd = client.customResourceDefinitions()
                .withName(ENTANDOPLUGIN_CRD_NAME).get();
        return client.customResources(entandoPluginCrd, EntandoPlugin.class, EntandoPluginList.class,
                DoneableEntandoPlugin.class);
    }

}
