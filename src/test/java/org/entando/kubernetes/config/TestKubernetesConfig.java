package org.entando.kubernetes.config;

import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.namespace.provider.FileBasedNamespaceProvider;
import org.entando.kubernetes.model.namespace.provider.NamespaceProvider;
import org.entando.kubernetes.model.namespace.provider.StaticNamespaceProvider;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestKubernetesConfig {

    @Value("${entando.namespaces.to.observe}")
    public List<String> namespacesToObserve;

    @Bean
    public NamespaceProvider namespaceProvider() {
        return new StaticNamespaceProvider("test-namespace");
    }

    @Bean
    public KubernetesClient kubernetesClient() {
        return Mockito.mock(KubernetesClient.class);
    }

    @Bean
    public ObservedNamespaces observedNamespaces() {
        return new MockObservedNamespaces(namespacesToObserve);
    }


}
