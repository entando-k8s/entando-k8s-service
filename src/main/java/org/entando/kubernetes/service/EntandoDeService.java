package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntandoDeService {

    private final KubernetesClient client;

    public EntandoDeService(@Autowired final KubernetesClient client) {
        this.client = client;
    }

    public List<EntandoDeBundle> getAllBundles() {
        return getBundleOperations().inAnyNamespace().list().getItems();
    }

    public List<EntandoDeBundle> getAllBundlesInNamespace(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoDeBundle> findBundlesByName(String bundleName) {
        return getAllBundles().stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByNameAndNamespace(String bundleName, String namespace) {
        return getAllBundlesInNamespace(namespace).stream()
                .filter(b -> b.getSpec().getDetails().getName().equals(bundleName))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords) {
        return getAllBundles().stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords) {
        return getAllBundles().stream()
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
