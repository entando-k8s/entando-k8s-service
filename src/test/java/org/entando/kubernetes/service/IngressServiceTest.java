package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.Optional;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.IngressTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
@EnableKubernetesMockClient(crud = true, https = false)
class IngressServiceTest {

    private IngressService ingressService;

    static KubernetesClient client;

    @BeforeEach
    public void setUp() {
        final KubernetesUtils ku = new KubernetesUtils(token -> client);
        ku.decode(KubernetesUtilsTest.NON_K8S_TOKEN);
        ingressService = new IngressService(ku);
    }

    @Test
    void shouldFindIngressForApp() {
        EntandoApp app = EntandoAppTestHelper.getTestEntandoApp();
        IngressTestHelper.createAppIngress(client, app);

        Optional<Ingress> appIngress = ingressService.findByEntandoApp(app);
        assertThat(appIngress).isPresent();
    }

    @Test
    void shouldFindIngressForPlugin() {
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        IngressTestHelper.createPluginIngress(client, plugin);

        Optional<Ingress> pluginIngress = ingressService.findByEntandoPlugin(plugin);
        assertThat(pluginIngress).isPresent();
    }
}
