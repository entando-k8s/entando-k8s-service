package org.entando.kubernetes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KubernetesUtils {

    public static final String DEFAULT_BUNDLE_NAMESPACE = "entando-de-bundles";
    public static final String KUBERNETES_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

    public static String getBundleDefaultNamespace() {
        Path namespacePath = Paths.get(KUBERNETES_NAMESPACE_PATH);
        String namespace = null;
        if (namespacePath.toFile().exists()) {
            try {
                namespace = new String(Files.readAllBytes(namespacePath));
            } catch (IOException e) {
                log.error(String.format("An error occurred while reading the namespace from file %s", namespacePath.toString()), e);
            }
        }
        return Optional.ofNullable(namespace).orElse(DEFAULT_BUNDLE_NAMESPACE);
    }

}
