package org.entando.kubernetes.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings({"sonar", "PMD"})
public final class KubernetesUtils {

    public static final String ENTANDO_BUNDLES_NAMESPACE_FALLBACK = "entando-de-bundles";
    public static final Path KUBERNETES_NAMESPACE_PATH =
            Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace");

    private static String namespace = null;
    private KubernetesUtils() {}

    public static String getBundleDefaultNamespace() {
        if (namespace == null) {
            if (KUBERNETES_NAMESPACE_PATH.toFile().exists()) {
                try {
                    namespace = new String(Files.readAllBytes(KUBERNETES_NAMESPACE_PATH), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("An error occurred while reading the namespace from file {}",
                            KUBERNETES_NAMESPACE_PATH.toString(), e);
                }
            }
            namespace = Optional.ofNullable(namespace).orElse(ENTANDO_BUNDLES_NAMESPACE_FALLBACK);
        }
        return namespace;
    }

}
