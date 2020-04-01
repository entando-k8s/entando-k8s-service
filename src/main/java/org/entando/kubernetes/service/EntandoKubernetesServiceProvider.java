package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Getter;
import org.entando.kubernetes.model.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Getter
@Service
public class EntandoKubernetesServiceProvider {

    private final EntandoAppService appService;
    private final EntandoPluginService pluginService;
    private final EntandoLinkService linkService;
    private final IngressService ingressService;
    private final EntandoDeBundleService bundleService;

    public EntandoKubernetesServiceProvider(KubernetesClient client, ObservedNamespaces observedNamespaces) {
        this.appService = new EntandoAppService(client, observedNamespaces);
        this.pluginService = new EntandoPluginService(client, observedNamespaces);
        this.linkService = new EntandoLinkService(client, observedNamespaces);
        this.ingressService = new IngressService(client, observedNamespaces);
        this.bundleService = new EntandoDeBundleService(client, observedNamespaces);
    }

}
