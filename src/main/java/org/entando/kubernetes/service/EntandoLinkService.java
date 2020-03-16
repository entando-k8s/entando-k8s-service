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
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.link.DoneableEntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkBuilder;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkList;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkOperationFactory;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoLinkService {

    private final KubernetesClient client;
    private final List<String> observedNamespaces;

    public EntandoLinkService(KubernetesClient client, List<String> observedNamespaces) {
        this.client = client;
        this.observedNamespaces = observedNamespaces;
    }

    public Optional<EntandoAppPluginLink> getLink(EntandoApp app, String pluginName) {
        return getLinksOperations().inNamespace(app.getMetadata().getNamespace())
                .list().getItems().stream()
                .filter(l -> l.getSpec().getEntandoPluginName().equals(pluginName))
                .findFirst();
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
        log.info("Link creation between EntandoApp {} on namespace {} and EntandoPlugin {} on namespace {}",
                newLink.getSpec().getEntandoAppName(), newLink.getSpec().getEntandoAppNamespace(),
                newLink.getSpec().getEntandoPluginName(), newLink.getSpec().getEntandoPluginNamespace());
        return getLinksOperations().inNamespace(newLink.getMetadata().getNamespace()).create(newLink);
    }

    public void delete(EntandoAppPluginLink l) {
        log.info("Deleting link between EntandoApp {} on namespace {} and EntandoPlugin {} on namespace {}",
                l.getSpec().getEntandoAppName(), l.getSpec().getEntandoAppNamespace(),
                l.getSpec().getEntandoPluginName(), l.getSpec().getEntandoPluginNamespace());
        getLinksOperations().inNamespace(l.getMetadata().getNamespace()).delete(l);
    }

    public EntandoAppPluginLink generateForAppAndPlugin(EntandoApp app, EntandoPlugin plugin) {
        String appNamespace = app.getMetadata().getNamespace();
        String appName = app.getMetadata().getName();
        String pluginName = plugin.getMetadata().getName();
        String pluginNamespace = plugin.getMetadata().getNamespace();
        return new EntandoAppPluginLinkBuilder()
                .withNewMetadata()
                .withName(String.format("%s-%s-link", appName, pluginName))
                .withNamespace(appNamespace)
                .endMetadata()
                .withNewSpec()
                .withEntandoApp(appNamespace, appName)
                .withEntandoPlugin(pluginNamespace, pluginName)
                .endSpec()
                .build();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoAppPluginLink, EntandoAppPluginLinkList, DoneableEntandoAppPluginLink, Resource<EntandoAppPluginLink, DoneableEntandoAppPluginLink>> getLinksOperations() {
        //CHECKSTYLE:ON
        return EntandoAppPluginLinkOperationFactory.produceAllEntandoAppPluginLinks(client);
    }

}
