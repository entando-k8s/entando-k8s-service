package org.entando.kubernetes.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.service.DefaultKubernetesClientBuilder;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.OperatorDeploymentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KubernetesConfiguration {

    @Value("${entando.namespaces.to.observe:}")
    public List<String> entandoNamespacesToObserve = new ArrayList<>();
    @Value("${entando.namespaces.of.interest:}")
    public List<String> entandoNamespacesOfInterest = new ArrayList<>();

    @Value("${entando.k8s.operator.deployment.type:helm}")
    public String deploymentType;
    private final KubernetesUtils kubernetesUtils = new KubernetesUtils(new DefaultKubernetesClientBuilder());

    @Bean
    public KubernetesUtils k8sUtils() {
        return kubernetesUtils;
    }

    @Bean
    public ObservedNamespaces observedNamespaces() {
        Set<String> accessibleNamespaces = new HashSet<>(entandoNamespacesOfInterest);
        accessibleNamespaces.addAll(entandoNamespacesToObserve);
        return new ObservedNamespaces(k8sUtils(), new ArrayList<>(accessibleNamespaces),
                OperatorDeploymentType.valueOf(deploymentType.toUpperCase(Locale.getDefault())));
    }

}
