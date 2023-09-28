package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.SecretTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
@EnableKubernetesMockClient(crud = true, https = false)
class SecretServiceTest {

    private SecretService secretService;

    static KubernetesClient client;

    KubernetesUtils ku;

    @BeforeEach
    public void setUp() {
        ku = new KubernetesUtils(token -> client);
        ku.decode(KubernetesUtilsTest.NON_K8S_TOKEN);
        secretService = new SecretService(ku);
    }

    @Test
    void shouldFindSecret() {
        String secretName = "pn-3a0eefc4-13d51bef-88bf4312-simple-ms-server-conf";
        String namespace = ku.getDefaultPluginNamespace();
        SecretTestHelper.createSecretWithData(client, namespace, secretName, Map.of());

        Optional<Secret> sec = secretService.findByName(secretName);
        assertThat(sec).isPresent();
        sec = secretService.findByName("noop");
        assertThat(sec).isNotPresent();
    }

    @Test
    void shouldGetValueFromSecret() {
        final String secretName = "pn-3a0eefc4-13d51bef-88bf4312-simple-ms-server-conf";
        final String namespace = ku.getDefaultPluginNamespace();
        final String key = "key";
        final String value = "value";

        SecretTestHelper.createSecretWithData(client, namespace, secretName, Map.of(key,
                Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8))));
        SecretTestHelper.createSecretWithStringData(client, namespace, secretName + "-string", Map.of(key, value));

        Optional<Secret> sec = secretService.findByName(secretName);
        assertThat(sec).isPresent();
        Optional<String> secValue = secretService.getValueFromSecret(sec.get(), key);
        assertThat(secValue).isPresent().contains(value);
        secValue = secretService.getValueFromSecret(sec.get(), "noop");
        assertThat(secValue).isNotPresent();

        sec = secretService.findByName(secretName + "-string");
        assertThat(sec).isPresent();
        secValue = secretService.getValueFromSecret(sec.get(), key);
        assertThat(secValue).isPresent().contains(value);

    }


}
