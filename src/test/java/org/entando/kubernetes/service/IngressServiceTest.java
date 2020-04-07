package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Optional;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.IngressTestHelper;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
public class IngressServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private IngressService ingressService;

    private KubernetesClient client;

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        ObservedNamespaces ons = new MockObservedNamespaces(
                Arrays.asList(TEST_APP_NAMESPACE, TEST_PLUGIN_NAMESPACE)
        );
        ingressService = new IngressService(client, ons);
        EntandoAppTestHelper.createEntandoAppCrd(client);
    }

    @Test
    public void shouldFindIngressForApp() {
        EntandoApp app = EntandoAppTestHelper.getTestEntandoApp();
        IngressTestHelper.createAppIngress(this.client, app);

        Optional<Ingress> appIngress = ingressService.findByEntandoApp(app);
        assertThat(appIngress).isPresent();
    }

    @Test
    public void shouldFindIngressForPlugin() {
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        IngressTestHelper.createPluginIngress(this.client, plugin);

        Optional<Ingress> pluginIngress = ingressService.findByEntandoPlugin(plugin);
        assertThat(pluginIngress).isPresent();
    }
}
