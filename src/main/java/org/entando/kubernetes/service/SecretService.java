package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SecretService {

    private final KubernetesUtils kubernetesUtils;

    public SecretService(KubernetesUtils kubernetesUtils) {
        this.kubernetesUtils = kubernetesUtils;
    }

    public Optional<Secret> findByName(String name) {
        final String namespace = kubernetesUtils.getDefaultPluginNamespace();

        Secret secret = getSecretOperations()
                .inNamespace(namespace)
                .withName(name).get();
        return Optional.ofNullable(secret);
    }

    public Optional<String> getValueFromSecret(Secret secret, String key) {
        return Optional.ofNullable(secret.getData())
                .map(data -> Optional.ofNullable(data.get(key))
                        .map(s -> new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8)))
                .orElseGet(() -> Optional.ofNullable(secret.getStringData())
                        .map(data -> data.get(key)));
    }

    //CHECKSTYLE:OFF
    private MixedOperation<Secret, SecretList, Resource<Secret>> getSecretOperations() {
        //CHECKSTYLE:ON
        return kubernetesUtils.getCurrentKubernetesClient().secrets();
    }

}
