package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.entando.kubernetes.model.namespace.provider.FileBasedNamespaceProvider;
import org.entando.kubernetes.model.namespace.provider.NamespaceProvider.NamespaceProviderException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class FileBaseNamespaceProviderTest {

    @Test
    public void shouldReadNamespaceFromFile() {
        Path path = Paths.get(this.getClass().getResource("/test-namespace").getPath());
        KubernetesUtils ku = new KubernetesUtils(new FileBasedNamespaceProvider(path));
        assertThat(ku.getCurrentNamespace()).isEqualTo("test-namespace");
    }

    @Test
    public void shouldThrowAnExceptionIfFileDoesNotExist() {
        Path path = Paths.get("my", "fake", "file");
        Assertions.assertThrows(NamespaceProviderException.class, () -> {
            new KubernetesUtils(new FileBasedNamespaceProvider(path));
        });
    }

}
