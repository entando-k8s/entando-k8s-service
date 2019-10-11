package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppService {

    private final @NonNull KubernetesClient client;

    public EntandoAppService(@Autowired final KubernetesClient client) {
        this.client = client;
    }

    public List<EntandoApp> listEntandoApps() {
        return getEntandoAppsOperations().inAnyNamespace().list().getItems();
    }

    public List<EntandoApp> listEntandoAppsInNamespace(String namespace) {
        return getEntandoAppsOperations().inNamespace(namespace).list().getItems();
    }

    public Optional<EntandoApp> findAppByName(String name) {
        return getEntandoAppsOperations().inAnyNamespace().list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(name)).findFirst();
    }

    public Optional<EntandoApp> findAppByNameAndNamespace(String name, String namespace) {
        return getEntandoAppsOperations().inNamespace(namespace).list().getItems().stream()
                .filter(pl -> pl.getMetadata().getName().equals(name)).findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoApp, EntandoAppList, DoneableEntandoApp, Resource<EntandoApp, DoneableEntandoApp>> getEntandoAppsOperations() {
        //CHECKSTYLE:ON
        CustomResourceDefinition entandoAppCrd = client.customResourceDefinitions()
                .withName(EntandoApp.CRD_NAME).get();
        return client.customResources(entandoAppCrd, EntandoApp.class, EntandoAppList.class,
                DoneableEntandoApp.class);
    }

}
