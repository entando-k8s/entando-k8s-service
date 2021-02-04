package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;

@Slf4j
@RequiredArgsConstructor
public abstract class EntandoKubernetesResourceCollector<T extends HasMetadata> {

    protected final KubernetesClient client;
    protected final ObservedNamespaces observedNamespaces;

    public List<T> getAll() {
        return collectFromNamespaces(observedNamespaces.getNames());
    }

    public List<T> getAllInNamespace(String namespace) {
        observedNamespaces.failIfNotObserved(namespace);
        return getInNamespaceWithoutChecking(namespace);
    }

    protected abstract List<T> getInNamespaceWithoutChecking(String namespace);

    @SuppressWarnings("unchecked")
    public List<T> collectFromNamespaces(List<String> namespaceList) {
        CompletableFuture<List<T>>[] allRequests = namespaceList.stream()
                .map(ns -> CompletableFuture.supplyAsync(() -> getAllInNamespace(ns))
                        .exceptionally(ex -> Collections.emptyList()))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<List<T>> collectedResources = CompletableFuture.allOf(allRequests)
                .thenApply(v -> Stream.of(allRequests).map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        return collectedResources.join();
    }


    public Optional<T> findByName(String name) {
        return getAll().stream().filter(r -> r.getMetadata().getName().equals(name)).findFirst();
    }

    public Optional<T> findByNameAndNamespace(String name, String namespace) {
        return getAllInNamespace(namespace)
                .stream()
                .filter(r -> r.getMetadata().getName().equals(name))
                .findFirst();
    }
}
