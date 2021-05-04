package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Optional;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.IngressTestHelper;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
class IngressServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private IngressService ingressService;

    private KubernetesClient client;

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        final KubernetesUtils ku = new KubernetesUtils(token -> server.getClient());
        ku.decode(KubernetesUtilsTest.K8S_TOKEN);
        ingressService = new IngressService(ku);
    }

    @Test
    void shouldFindIngressForApp() {
        EntandoApp app = EntandoAppTestHelper.getTestEntandoApp();
        IngressTestHelper.createAppIngress(this.client, app);

        Optional<Ingress> appIngress = ingressService.findByEntandoApp(app);
        assertThat(appIngress).isPresent();
    }

    @Test
    void shouldFindIngressForPlugin() {
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        IngressTestHelper.createPluginIngress(this.client, plugin);

        Optional<Ingress> pluginIngress = ingressService.findByEntandoPlugin(plugin);
        assertThat(pluginIngress).isPresent();
    }
}
