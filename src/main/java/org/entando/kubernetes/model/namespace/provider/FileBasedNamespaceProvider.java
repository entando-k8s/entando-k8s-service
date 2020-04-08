package org.entando.kubernetes.model.namespace.provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileBasedNamespaceProvider implements NamespaceProvider {
    private final Path DEFAULT_KUBERNETES_NAMESPACE_PATH =
            Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/namespace");

    private Path namespaceFilePath;
    private String namespace;

    public FileBasedNamespaceProvider() {
        this.namespaceFilePath = DEFAULT_KUBERNETES_NAMESPACE_PATH;
    }

    public FileBasedNamespaceProvider(Path path) {
        if (path == null || !path.toFile().exists()) {
            throw new NamespaceProviderException("Provided path is null or doesn't exists on the system");
        }
        this.namespaceFilePath = path;
    }

    @Override
    public String getNamespace() {
        if (namespaceFilePath.toFile().exists()) {
            try {
                namespace = new String(Files.readAllBytes(namespaceFilePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("An error occurred while reading the namespace from file {}",
                        namespaceFilePath.toString(), e);
            }
        }
        return Optional.ofNullable(namespace).orElseThrow(() -> new RuntimeException("Impossible to identify current k8s namespace"));
    }

}
