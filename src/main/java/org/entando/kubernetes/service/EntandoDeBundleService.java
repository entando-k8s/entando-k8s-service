package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.debundle.EntandoDeBundleOperationFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService extends EntandoKubernetesResourceCollector<EntandoDeBundle> {

    public EntandoDeBundleService(KubernetesUtils kubernetesUtils,
            ObservedNamespaces observedNamespaces) {
        super(kubernetesUtils, observedNamespaces);
    }

    @Override
    protected List<EntandoDeBundle> getInAnyNamespace() {
        return getBundleOperations().inAnyNamespace().list().getItems();
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

    public EntandoDeBundle createBundle(EntandoDeBundle entandoDeBundle) {
        return getBundleOperations()
                .inNamespace(entandoDeBundle.getMetadata().getNamespace()).createOrReplace(entandoDeBundle);
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle,
            DoneableEntandoDeBundle>> getBundleOperations() {
        //CHECKSTYLE:ON
        return EntandoDeBundleOperationFactory.produceAllEntandoDeBundles(kubernetesUtils.getCurrentKubernetesClient());
    }

}
