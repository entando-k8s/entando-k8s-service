package org.entando.kubernetes.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@SuppressWarnings({"sonar", "PMD"})
@Component
public class KubernetesUtils {

    public final Path KUBERNETES_NAMESPACE_PATH =
            Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace");

    private String namespace = null;

    public String getCurrentNamespace() {
        if (namespace == null) {
            if (KUBERNETES_NAMESPACE_PATH.toFile().exists()) {
                try {
                    namespace = new String(Files.readAllBytes(KUBERNETES_NAMESPACE_PATH), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("An error occurred while reading the namespace from file {}",
                            KUBERNETES_NAMESPACE_PATH.toString(), e);
                }
            }
            namespace = Optional.ofNullable(namespace).orElseThrow(() -> new RuntimeException("Impossible to identify current k8s namespace"));
        }
        return namespace;
    }

}
