package org.entando.kubernetes.config;

import java.util.List;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.OperatorDeploymentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestKubernetesConfig {

    @Value("${entando.namespaces.to.observe}")
    public List<String> namespacesToObserve;

    @Bean
    public ObservedNamespaces observedNamespaces() {
        return new ObservedNamespaces(k8sUtils(), namespacesToObserve, OperatorDeploymentType.HELM);
    }

    @Bean
    public KubernetesUtils k8sUtils() {
        final KubernetesUtils kubernetesUtils = new KubernetesUtils() {
            @Override
            public String getCurrentNamespace() {
                return "test-namespace";
            }
        };
        return kubernetesUtils;
    }

}
