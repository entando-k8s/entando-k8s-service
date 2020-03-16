package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.zjsonpatch.internal.guava.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.model.plugin.EntandoPluginOperationFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntandoPluginService {

    private final KubernetesClient client;
    private final List<String> observedNamespaces;
    private final KubernetesUtils k8sUtils;

    public void deletePlugin(String pluginId) {
        log.info("Delete plugin {} from observed namespaces", pluginId);
        Optional<EntandoPlugin> entandoPlugin = findPluginByName(pluginId);
        entandoPlugin.ifPresent(this::deletePlugin);
    }

    public void deletePlugin(EntandoPlugin plugin) {
        log.info("Delete plugin {} from observed namespaces", plugin.getMetadata().getName());
        getPluginOperations().delete(plugin);
    }

    public void deletePluginInNamespace(String pluginId, String namespace) {
        log.info("Delete plugin {} from namespace {}", pluginId, namespace);
        EntandoPlugin pluginToRemove = new EntandoPlugin();
        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName(pluginId).withNamespace(namespace).build();
        pluginToRemove.setMetadata(pluginMeta);
        getPluginOperations().inAnyNamespace().delete(pluginToRemove);
    }

    public List<EntandoPlugin> getPlugins() {
        return getPluginsInNamespaceList(observedNamespaces);
    }

    public List<EntandoPlugin> getPluginsInNamespace(String namespace) {
        return getPluginOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoPlugin> getPluginsInNamespaceList(List<String> namespaceList) {
        CompletableFuture<List<EntandoPlugin>>[] allRequests = namespaceList.stream()
                .map(ns -> CompletableFuture.supplyAsync(() -> getPluginsInNamespace(ns) ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<List<EntandoPlugin>> allPlugins = CompletableFuture.allOf(allRequests)
                .thenApply(v -> Stream.of(allRequests).map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    log.error("An error occurred while retrieving bundles from multiple namespaces", ex);
                    return Collections.emptyList();
                });

        return allPlugins.join();
    }

    public EntandoPlugin deploy(EntandoPlugin plugin) {
        log.info("Deploying plugin {} in namespace {}", plugin.getMetadata().getName(),
                plugin.getMetadata().getNamespace());
        EntandoPlugin cleanPlugin = pluginCleanUp(plugin);
        if (!this.observedNamespaces.contains(cleanPlugin.getMetadata().getNamespace())) {
            throw BadRequestExceptionFactory.pluginNamespaceNotObserved(cleanPlugin);
        }
        return getPluginOperations().inNamespace(cleanPlugin.getMetadata().getNamespace()).create(cleanPlugin);
    }

    private EntandoPlugin pluginCleanUp(EntandoPlugin plugin) {
        //TODO verify the plugin has a name
        //assert !Strings.isNullOrEmpty(plugin.getMetadata().getName());
        if (Strings.isNullOrEmpty(plugin.getMetadata().getNamespace())) {
            plugin.getMetadata().setNamespace(k8sUtils.getCurrentNamespace());
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


    public Optional<EntandoPlugin> findPluginByName(String pluginId) {
        return getPlugins().stream().filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    public Optional<EntandoPlugin> findPluginByIdAndNamespace(String pluginId, String namespace) {
        return getPluginsInNamespace(namespace).stream().filter(pl -> pl.getMetadata().getName().equals(pluginId)).findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> getPluginOperations() {
        //CHECKSTYLE:ON
        return EntandoPluginOperationFactory.produceAllEntandoPlugins(client);
    }

}
