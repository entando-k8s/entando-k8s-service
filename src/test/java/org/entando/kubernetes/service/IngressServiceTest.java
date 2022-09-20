package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.networking.v1.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.ServerStatus;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
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

    @Test
    void shouldNotRemoveIngressPathForPlugin() {
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPluginNoIngressPath();

        // no link no ingressName no path
        Map<String, Boolean> res = ingressService.deletePathFromIngressByEntandoPlugin(plugin, Collections.EMPTY_LIST);
        assertThat(res).isEmpty();

        // link no ingressName no path
        Ingress ingress = IngressTestHelper.createPluginIngress(client, plugin);
        ServerStatus mainServerStatus = new ServerStatus("main");
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLinkWithServerStatus(client,
                mainServerStatus);
        res = ingressService.deletePathFromIngressByEntandoPlugin(plugin, Collections.singletonList(testLink));
        assertThat(res).isEmpty();
 
        // link ingressName no path
        ingress = IngressTestHelper.createPluginIngress(client, plugin);
        mainServerStatus = new ServerStatus("main");
        mainServerStatus.setIngressName(ingress.getMetadata().getName());
        testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLinkWithServerStatus(client,
                mainServerStatus);
        res = ingressService.deletePathFromIngressByEntandoPlugin(plugin, Collections.singletonList(testLink));
        assertThat(res).isNotEmpty().containsKeys(ingress.getMetadata().getName());
        assertThat(res.get(ingress.getMetadata().getName())).isFalse();

    }

    @Test
    void shouldRemoveIngressPathForPlugin() {
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        IngressRule rule = new IngressRule("host.com",
                new HTTPIngressRuleValue(
                        Arrays.asList(
                                new HTTPIngressPath(null, "/dummyPlugin", "Prefix"),
                                new HTTPIngressPath(null, "/dummyCustomPath", "Prefix"))));
        Map<String, String> annotations = new HashMap<>();
        annotations.put("entando.org/quickstart-pn-dummy-plugin-path", "/dummyPlugin");
        annotations.put("entando.org/quickstart-pn-dummy-custom-plugin-path", "/dummyCustomPath");
        Ingress ingress = IngressTestHelper.createPluginIngressWithRuleAndAnnotation(client, plugin, rule, annotations);

        ServerStatus mainServerStatus = new ServerStatus("main");
        mainServerStatus.setIngressName(ingress.getMetadata().getName());

        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLinkWithServerStatus(
                client, mainServerStatus);

        Map<String, Boolean> res = ingressService.deletePathFromIngressByEntandoPlugin(plugin,
                Collections.singletonList(testLink));

        assertThat(res).isNotEmpty().containsKeys(ingress.getMetadata().getName());
        assertThat(res.get(ingress.getMetadata().getName())).isTrue();

    }

}
