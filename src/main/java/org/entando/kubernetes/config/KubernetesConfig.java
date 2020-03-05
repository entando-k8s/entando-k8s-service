package org.entando.kubernetes.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.entando.kubernetes.service.KubernetesUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KubernetesConfig {

    @Value("${entando.namespaces.to.observe:}")
    public List<String> entandoNamespacesToObserve = new ArrayList<>();

    @Bean
    public KubernetesClient client() {
        final Config config = new ConfigBuilder().build();
        return new DefaultKubernetesClient(config);
    }

    @Bean
    public List<String> observedNamespaces() {
       if (entandoNamespacesToObserve != null && !entandoNamespacesToObserve.isEmpty()) {
           return entandoNamespacesToObserve;
       }
       return Collections.singletonList(KubernetesUtils.getCurrentNamespace());
    }

}
