package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.DoneableEntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppPluginLinkService {

    private final @NonNull KubernetesClient client;

    public AppPluginLinkService(@Autowired final KubernetesClient client) {
        this.client = client;
    }

    public List<EntandoAppPluginLink> listAppLinks(EntandoApp app) {
        return getLinksOperations().inNamespace(app.getMetadata().getNamespace()).list().getItems();
    }

    public List<EntandoAppPluginLink> listEntandoAppLinks(String namespace, String name) {
        return getLinksOperations().inNamespace(namespace).list().getItems()
                .stream().filter(el -> el.getSpec().getEntandoAppName().equals(name))
                .collect(Collectors.toList());
    }

    public EntandoAppPluginLink deploy(EntandoAppPluginLink newLink) {
        return getLinksOperations().inNamespace(newLink.getMetadata().getNamespace()).create(newLink);
    }

    public void delete(EntandoAppPluginLink l) {
        getLinksOperations().inNamespace(l.getMetadata().getNamespace()).delete(l);
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoAppPluginLink, EntandoAppPluginLinkList, DoneableEntandoAppPluginLink, Resource<EntandoAppPluginLink, DoneableEntandoAppPluginLink>> getLinksOperations() {
        //CHECKSTYLE:ON
        CustomResourceDefinition linkCrd = client.customResourceDefinitions()
                .withName(EntandoAppPluginLink.CRD_NAME).get();
        return client.customResources(linkCrd, EntandoAppPluginLink.class, EntandoAppPluginLinkList.class,
                DoneableEntandoAppPluginLink.class);
    }

}
