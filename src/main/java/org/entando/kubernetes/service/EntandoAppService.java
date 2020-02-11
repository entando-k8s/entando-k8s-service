package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppList;
import org.entando.kubernetes.model.app.EntandoAppOperationFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoAppService {

    private final KubernetesClient client;
    private final List<String> observedNamespaces;

    public EntandoAppService(KubernetesClient client, List<String> observedNamespaces) {
        this.client = client;
        this.observedNamespaces = observedNamespaces;

    }

    public List<EntandoApp> getApps() {
        return getAppsInNamespaceList(observedNamespaces);
    }

    public List<EntandoApp> getAppsInNamespace(String namespace) {
        return getEntandoAppsOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoApp> getAppsInNamespaceList(List<String> namespaceList) {
        CompletableFuture<List<EntandoApp>>[] allRequests = namespaceList.stream()
                .map(ns -> CompletableFuture.supplyAsync(() -> getAppsInNamespace(ns) ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<List<EntandoApp>> allEntandoApps = CompletableFuture.allOf(allRequests)
                .thenApply(v -> Stream.of(allRequests).map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    log.error("An error occurred while retrieving bundles from multiple namespaces", ex);
                    return Collections.emptyList();
                });

        return allEntandoApps.join();
    }


    public Optional<EntandoApp> findAppByName(String name) {
        return getApps().stream().filter(pl -> pl.getMetadata().getName().equals(name)).findFirst();
    }

    public Optional<EntandoApp> findAppByNameAndNamespace(String name, String namespace) {
        return getAppsInNamespace(namespace).stream()
                .filter(pl -> pl.getMetadata().getName().equals(name)).findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoApp, EntandoAppList, DoneableEntandoApp, Resource<EntandoApp, DoneableEntandoApp>> getEntandoAppsOperations() {
        //CHECKSTYLE:ON
        return EntandoAppOperationFactory.produceAllEntandoApps(client);
    }

}
