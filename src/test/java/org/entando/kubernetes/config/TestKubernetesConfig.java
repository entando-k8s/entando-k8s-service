package org.entando.kubernetes.config;

import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import org.entando.kubernetes.model.ObservedNamespaces;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestKubernetesConfig {

    private final String TEST_CURRENT_NAMESPACE = "test-namespace";

    @Value("${entando.namespaces.to.observe}")
    public List<String> namespacesToObserve;

    @Bean
    public KubernetesUtils k8sUtils() {
        KubernetesUtils mockedK8sUtils = Mockito.mock(KubernetesUtils.class);
        when(mockedK8sUtils.getCurrentNamespace()).thenReturn(TEST_CURRENT_NAMESPACE);
        return mockedK8sUtils;
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
