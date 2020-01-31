package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntandoDeBundleService {

    private final KubernetesClient client;

    public EntandoDeBundleService(@Autowired final KubernetesClient client) {
        this.client = client;
    }


    public List<EntandoDeBundle> getAllBundlesInDefaultNamespace() {
        return getBundleOperations().inNamespace(KubernetesUtils.getBundleDefaultNamespace()).list().getItems();
    }

    public List<EntandoDeBundle> getAllBundlesInNamespace(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoDeBundle> findBundlesByName(String bundleName) {
        return getAllBundlesInDefaultNamespace().stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .collect(Collectors.toList());
    }

    public Optional<EntandoDeBundle> findBundleByNameAndNamespace(String bundleName, String namespace) {
        return getAllBundlesInNamespace(namespace).stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .findFirst();
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords) {
        return getAllBundlesInDefaultNamespace().stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords) {
        return getAllBundlesInDefaultNamespace().stream()
                .filter(b -> keywords.containsAll(b.getSpec().getDetails().getKeywords()))
                .collect(Collectors.toList());
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle, DoneableEntandoDeBundle>> getBundleOperations() {
        //CHECKSTYLE:ON
        CustomResourceDefinition entandoDeBundleCrd = client.customResourceDefinitions()
                .withName(EntandoDeBundle.CRD_NAME).get();
        return client.customResources(entandoDeBundleCrd, EntandoDeBundle.class, EntandoDeBundleList.class,
                DoneableEntandoDeBundle.class);
    }

}
