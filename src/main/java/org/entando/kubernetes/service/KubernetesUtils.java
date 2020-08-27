package org.entando.kubernetes.service;

import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.namespace.provider.NamespaceProvider;

@Slf4j
@SuppressWarnings({"sonar", "PMD"})
public class KubernetesUtils {

    private NamespaceProvider provider;

    private String namespace = null;

    public KubernetesUtils(NamespaceProvider provider) {
        this.provider = provider;
    }

    public String getCurrentNamespace() {
        if (namespace == null) {
            namespace = provider.getNamespace();
        }
        return namespace;
    }

}
