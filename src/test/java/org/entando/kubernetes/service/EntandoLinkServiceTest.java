package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.Arrays;
import java.util.List;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tags({@Tag("component"), @Tag("in-process")})
@EnableRuleMigrationSupport
@EnableKubernetesMockClient(crud = true, https = false)
class EntandoLinkServiceTest {

    private EntandoLinkService linkService;

    static KubernetesClient client;

    @BeforeEach
    void setUp() {
        EntandoLinkTestHelper.deleteInAllNamespaces(client);
    }

    private void initializeService(String... namespaces) {
        KubernetesUtils kubernetesUtils = new KubernetesUtils(token -> client);
        kubernetesUtils.decode(KubernetesUtilsTest.NON_K8S_TOKEN);
        ObservedNamespaces ons = new ObservedNamespaces(kubernetesUtils, Arrays.asList(namespaces),
                OperatorDeploymentType.HELM);
        linkService = new EntandoLinkService(kubernetesUtils, ons);
    }

    @Test
    void shouldFindAllLinks() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        List<EntandoAppPluginLink> links = linkService.getAll();
        assertThat(links).hasSize(1);
    }

    @Test
    void shouldFindAllLinksInNamespace() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        List<EntandoAppPluginLink> links = linkService.getAllInNamespace(TEST_APP_NAMESPACE);
        assertThat(links).hasSize(1);
    }

    @Test
    void shouldNotFindAnyLinkIfNoAppIsAvailable() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        assertTrue(linkService.getAppLinks(testApp).isEmpty());
    }

    @Test
    void shouldNotFindAnyLinkIfAppHasNoLink() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertTrue(linkService.getAppLinks(testApp).isEmpty());
    }

    @Test
    void shouldFindLinkAssociatedWithApp() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        EntandoApp testApp = new EntandoAppBuilder()
                .withNewMetadata()
                .withName(testLink.getSpec().getEntandoAppName())
                .withNamespace(testLink.getSpec().getEntandoAppNamespace().get())
                .endMetadata()
                .build();
        assertEquals(1, linkService.getAppLinks(testApp).size());
    }

    @Test
    void shouldFindLinksAssociatedWithPlugin() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        EntandoPlugin testPlugin = new EntandoPluginBuilder()
                .withNewMetadata()
                .withName(testLink.getSpec().getEntandoPluginName())
                .endMetadata()
                .build();
        assertEquals(1, linkService.getPluginLinks(testPlugin).size());
    }

    @Test
    void shouldCreateLinkBetweenAppAndPlugin() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.getTestLink();

        EntandoAppPluginLink createdLink = linkService.deploy(testLink);
        assertEquals(String.format("%s-to-%s-link", TEST_APP_NAME, TEST_PLUGIN_NAME),
                createdLink.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getMetadata().getNamespace());
        assertEquals(TEST_APP_NAME, createdLink.getSpec().getEntandoAppName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getSpec().getEntandoAppNamespace().get());
        assertEquals(TEST_PLUGIN_NAME, createdLink.getSpec().getEntandoPluginName());
        assertEquals(TEST_PLUGIN_NAMESPACE, createdLink.getSpec().getEntandoPluginNamespace().get());

        //        assertEquals(1, linkService.listEntandoAppLinks(TEST_APP_NAMESPACE, TEST_APP_NAME).size());
    }

    @Test
    void shouldGenerateLinkWithAppropriateInfoFromAppAndPlugin() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        // overriding plugin namespace because it is read by the fake decoded JWT
        ep.getMetadata().setNamespace("test");

        EntandoAppPluginLink generatedLink = linkService.buildBetweenAppAndPlugin(ea, ep);
        assertEquals(ea.getMetadata().getName(), generatedLink.getSpec().getEntandoAppName());
        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoAppNamespace().get());
        assertEquals(ep.getMetadata().getName(), generatedLink.getSpec().getEntandoPluginName());
        assertEquals(ep.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoPluginNamespace().get());

        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getMetadata().getNamespace());
        assertEquals(
                String.format("%s-%s-link", ea.getMetadata().getName(), ep.getMetadata().getName()),
                generatedLink.getMetadata().getName());
    }

    @Test
    void shouldTruncateLinkWithNameWhenItExceeds63Chars() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        ep.getMetadata().setName("very-very-very-very-very-very-very-very-very-very-very-long-plugin-name");

        EntandoAppPluginLink generatedLink = linkService.buildBetweenAppAndPlugin(ea, ep);
        assertEquals(
                "my-app-very-very-very-very-very-very-very-very-very-very-v-link",
                generatedLink.getMetadata().getName());
    }

    @Test
    void shouldDeleteEntandoAppPluginLink() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoAppPluginLink el = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        linkService.delete(el);
        List<EntandoAppPluginLink> links = EntandoLinkService.getLinksOperations(client)
                .inNamespace(el.getMetadata().getNamespace())
                .list().getItems();
        assertTrue(links.isEmpty());
    }

    @Test
    void shouldNotThrowIfDeletingNonExistingPlugin() {
        initializeService(TEST_APP_NAMESPACE);
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        Assertions.assertDoesNotThrow(() -> linkService.delete(el));
    }
}
