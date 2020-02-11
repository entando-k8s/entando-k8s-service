package org.entando.kubernetes.config;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestKubernetesConfig {

    @Value("${entando.namespaces.to.observe}")
    public List<String> namespacesToObserve;

    @Bean
    public KubernetesClient kubernetesClient() {
        return Mockito.mock(KubernetesClient.class);
    }

    public List<String> observedNamespaces() {
        return namespacesToObserve;
    }

}
