package org.entando.kubernetes.config;

import io.fabric8.kubernetes.client.KubernetesClient;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class KubernetesConfig {

    @Bean
    public KubernetesClient client() {
        return Mockito.mock(KubernetesClient.class);
    }

}
