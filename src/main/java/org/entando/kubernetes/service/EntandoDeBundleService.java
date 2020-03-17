package org.entando.kubernetes.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.ObservedNamespaces;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.debundle.EntandoDeBundleOperationFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService extends EntandoKubernetesResourceCollector<EntandoDeBundle>{

    public EntandoDeBundleService(KubernetesClient client,
            ObservedNamespaces observedNamespaces) {
        super(client, observedNamespaces);
    }

    @Override
    protected List<EntandoDeBundle> getInNamespaceWithoutChecking(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords) {
        return getAll().stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords) {
        return getAll().stream()
                .filter(b -> keywords.containsAll(b.getSpec().getDetails().getKeywords()))
                .collect(Collectors.toList());
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle, DoneableEntandoDeBundle>> getBundleOperations() {
        //CHECKSTYLE:ON
        return EntandoDeBundleOperationFactory.produceAllEntandoDeBundles(client);
    }

}
