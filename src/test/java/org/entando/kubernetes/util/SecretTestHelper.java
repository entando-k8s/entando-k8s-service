package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;

public class SecretTestHelper {
    
    public static Secret createSecretWithData(KubernetesClient client, String namespace, String name, Map<String,String> data) {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .withData(data)
                .build();
        return client.secrets().inNamespace(namespace).create(secret);
    }

    public static Secret createSecretWithStringData(KubernetesClient client,
                                                    String namespace, String name, Map<String,String> stringData) {
        Secret secret = new SecretBuilder()
                .withNewMetadata()
                .withName(name)
                .withNamespace(namespace)
                .endMetadata()
                .withStringData(stringData)
                .build();
        return client.secrets().inNamespace(namespace).create(secret);
    }

}
