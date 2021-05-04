package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.function.Function;

public class DefaultKubernetesClientBuilder implements Function<String, KubernetesClient> {

    public static final String NOT_K8S_TOKEN = "NOT_K8S_TOKEN";

    @Override
    public KubernetesClient apply(String token) {
        // TODO should we remove this now that we don't support client credentials flow anymore?
        if (NOT_K8S_TOKEN.equals(token)) {
            return new DefaultKubernetesClient();
        } else {
            return new DefaultKubernetesClient(
                    //Get default config from current Kube context
                    new ConfigBuilder(Config.autoConfigure(null))
                            .withOauthToken(token)
                            .build());

        }
    }
}
