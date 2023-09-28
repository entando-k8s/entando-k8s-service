package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkBuilder;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoLinkService extends EntandoKubernetesResourceCollector<EntandoAppPluginLink> {

    public static final String LINK_SUFFIX = "-link";
    public static final int MAX_LINK_NAME_LENGTH = 63 - LINK_SUFFIX.length();

    public EntandoLinkService(KubernetesUtils kubernetesUtils,
            ObservedNamespaces observedNamespaces) {
        super(kubernetesUtils, observedNamespaces);
    }

    @Override
    protected List<EntandoAppPluginLink> getInAnyNamespace() {
        return getLinksOperations().inAnyNamespace().list().getItems();
    }

    @Override
    protected List<EntandoAppPluginLink> getInNamespaceWithoutChecking(String namespace) {
        return getLinksOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoAppPluginLink> findByAppName(String appName) {
        return getAll().stream()
                .filter(l -> l.getSpec().getEntandoAppName().equals(appName))
                .collect(Collectors.toList());
    }

    public List<EntandoAppPluginLink> findByPluginName(String pluginName) {
        return getAll().stream()
                .filter(l -> l.getSpec().getEntandoPluginName().equals(pluginName))
                .collect(Collectors.toList());
    }

    public List<EntandoAppPluginLink> getAppLinks(EntandoApp app) {
        return getLinksOperations().inNamespace(app.getMetadata().getNamespace()).list().getItems();
    }

    public List<EntandoAppPluginLink> getPluginLinks(EntandoPlugin plugin) {
        return getAll()
                .stream()
                .filter(l -> l.getSpec().getEntandoPluginName().equals(plugin.getMetadata().getName()))
                .collect(Collectors.toList());
    }

    public EntandoAppPluginLink deploy(EntandoAppPluginLink newLink) {
        log.info("Link creation between EntandoApp {} on namespace {} and EntandoPlugin {} on namespace {} for tenant {}",
                newLink.getSpec().getEntandoAppName(), newLink.getSpec().getEntandoAppNamespace(),
                newLink.getSpec().getEntandoPluginName(), newLink.getSpec().getEntandoPluginNamespace(),
                newLink.getSpec().getTenantCode());
        return getLinksOperations().inNamespace(newLink.getMetadata().getNamespace()).createOrReplace(newLink);
    }

    public void delete(EntandoAppPluginLink l) {
        log.info("Deleting link between EntandoApp {} on namespace {} and EntandoPlugin {} on namespace {} for tenant {}",
                l.getSpec().getEntandoAppName(), l.getSpec().getEntandoAppNamespace(),
                l.getSpec().getEntandoPluginName(), l.getSpec().getEntandoPluginNamespace(),
                l.getSpec().getTenantCode());
        getLinksOperations().inNamespace(l.getMetadata().getNamespace()).delete(l);
    }

    public EntandoAppPluginLink buildBetweenAppAndPlugin(EntandoApp app, EntandoPlugin plugin) {
        String appNamespace = app.getMetadata().getNamespace();
        String appName = app.getMetadata().getName();
        String pluginName = plugin.getMetadata().getName();
        String pluginNamespace = kubernetesUtils.getDefaultPluginNamespace();
        String tenantCode = plugin.getSpec().getTenantCode();
        return new EntandoAppPluginLinkBuilder()
                .withNewMetadata()
                .withName(createAppPluginLinkName(appName, pluginName))
                .withNamespace(appNamespace)
                .endMetadata()
                .withNewSpec()
                .withEntandoApp(appNamespace, appName)
                .withEntandoPlugin(pluginNamespace, pluginName)
                .withTenantCode(tenantCode)
                .endSpec()
                .build();
    }

    private String createAppPluginLinkName(String appName, String pluginName) {
        String baseName = String.format("%s-%s", appName, pluginName);
        if (baseName.length() > MAX_LINK_NAME_LENGTH) {   // max label value length = 63
            baseName = baseName.substring(0, MAX_LINK_NAME_LENGTH);
        }
        return baseName + LINK_SUFFIX;
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoAppPluginLink, KubernetesResourceList<EntandoAppPluginLink>, Resource<EntandoAppPluginLink>> getLinksOperations() {
        return getLinksOperations(kubernetesUtils.getCurrentKubernetesClient());
    }

    //CHECKSTYLE:OFF
    public static MixedOperation<EntandoAppPluginLink, KubernetesResourceList<EntandoAppPluginLink>, Resource<EntandoAppPluginLink>> getLinksOperations(
            KubernetesClient client) {
        return client.customResources(EntandoAppPluginLink.class);
    }

}
