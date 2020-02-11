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
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.debundle.EntandoDeBundleOperationFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService {

    private final KubernetesClient client;
    private final List<String> observedNamespaces;

    public EntandoDeBundleService(KubernetesClient client, List<String> observedNamespaces) {
        this.client = client;
        this.observedNamespaces = observedNamespaces;
    }


    public List<EntandoDeBundle> getBundles() {
        return getBundlesInNamespaceList(observedNamespaces);
    }

    public List<EntandoDeBundle> getBundlesInNamespace(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoDeBundle> getBundlesInNamespaceList(List<String> namespaceList) {
        CompletableFuture<List<EntandoDeBundle>>[] allRequests = namespaceList.stream()
                .map(ns -> CompletableFuture.supplyAsync(() -> getBundlesInNamespace(ns) ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture<List<EntandoDeBundle>> allBundles = CompletableFuture.allOf(allRequests)
                .thenApply(v -> Stream.of(allRequests).map(CompletableFuture::join)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()))
                .exceptionally(ex -> {
                    log.error("An error occurred while retrieving bundles from multiple namespaces", ex);
                    return Collections.emptyList();
                });

        return allBundles.join();
    }

    public List<EntandoDeBundle> findBundlesByName(String bundleName) {
        return getBundles().stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .collect(Collectors.toList());
    }

    public Optional<EntandoDeBundle> findBundleByNameAndNamespace(String bundleName, String namespace) {
        return getBundlesInNamespace(namespace).stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .findFirst();
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords) {
        return getBundles().stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords) {
        return getBundles().stream()
                .filter(b -> keywords.containsAll(b.getSpec().getDetails().getKeywords()))
                .collect(Collectors.toList());
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle, DoneableEntandoDeBundle>> getBundleOperations() {
        //CHECKSTYLE:ON
        return EntandoDeBundleOperationFactory.produceAllEntandoDeBundles(client);
    }

}
