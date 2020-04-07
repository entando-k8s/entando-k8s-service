package org.entando.kubernetes.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.namespace.provider.NamespaceProvider;
import org.springframework.stereotype.Component;

@Slf4j
@SuppressWarnings({"sonar", "PMD"})
@Component
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
