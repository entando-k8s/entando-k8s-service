package org.entando.kubernetes.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class KubernetesConfig {

    @Bean
    public KubernetesClient client() {
        final Config config = new ConfigBuilder().build();
        return new DefaultKubernetesClient(config);
    }

}
