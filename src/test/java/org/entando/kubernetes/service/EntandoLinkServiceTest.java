package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Collections;
import java.util.List;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
public class EntandoLinkServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoLinkService linkService;

    private KubernetesClient client;

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        ObservedNamespaces ons = new MockObservedNamespaces(Collections.singletonList(TEST_APP_NAMESPACE));
        linkService = new EntandoLinkService(client, ons);
        EntandoLinkTestHelper.createEntandoAppPluginLinkCrd(client);
        EntandoAppTestHelper.createEntandoAppCrd(client);
        EntandoPluginTestHelper.createEntandoPluginCrd(client);
    }

    @Test
    public void shouldFindAllLinks() {
       EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
       List<EntandoAppPluginLink> links = linkService.getAll();
       assertThat(links).hasSize(1);
    }

    @Test
    public void shouldFindAllLinksInNamespace() {
        EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        List<EntandoAppPluginLink> links = linkService.getAllInNamespace(TEST_APP_NAMESPACE);
        assertThat(links).hasSize(1);
    }

    @Test
    public void shouldNotFindAnyLinkIfNoAppIsAvailable() {
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        assertTrue(linkService.getAppLinks(testApp).isEmpty());
    }
    @Test
    public void shouldNotFindAnyLinkIfAppHasNoLink() {
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertTrue(linkService.getAppLinks(testApp).isEmpty());
    }

    @Test
    public void shouldFindLinkAssociatedWithApp() {
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        EntandoApp testApp = new EntandoAppBuilder()
                .withNewMetadata()
                .withName(testLink.getSpec().getEntandoAppName())
                .withNamespace(testLink.getSpec().getEntandoAppNamespace())
                .endMetadata()
                .build();
        assertEquals(1, linkService.getAppLinks(testApp).size());
    }

    @Test
    public void shouldFindLinksAssociatedWithPlugin() {
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        EntandoPlugin testPlugin = new EntandoPluginBuilder()
                .withNewMetadata()
                .withName(testLink.getSpec().getEntandoPluginName())
                .endMetadata()
                .build();
        assertEquals(1, linkService.getPluginLinks(testPlugin).size());
    }

    @Test
    public void shouldCreateLinkBetweenAppAndPlugin() {
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.getTestLink();

        EntandoAppPluginLink createdLink = linkService.deploy(testLink);
        assertEquals(String.format("%s-to-%s-link", TEST_APP_NAME, TEST_PLUGIN_NAME), createdLink.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getMetadata().getNamespace());
        assertEquals(TEST_APP_NAME, createdLink.getSpec().getEntandoAppName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getSpec().getEntandoAppNamespace());
        assertEquals(TEST_PLUGIN_NAME, createdLink.getSpec().getEntandoPluginName());
        assertEquals(TEST_PLUGIN_NAMESPACE, createdLink.getSpec().getEntandoPluginNamespace());

//        assertEquals(1, linkService.listEntandoAppLinks(TEST_APP_NAMESPACE, TEST_APP_NAME).size());
    }

    @Test
    public void shouldGenerateLinkWithAppropriateInfoFromAppAndPlugin() {
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();

        EntandoAppPluginLink generatedLink = linkService.buildBetweenAppAndPlugin(ea, ep);
        assertEquals(ea.getMetadata().getName(), generatedLink.getSpec().getEntandoAppName());
        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoAppNamespace());
        assertEquals(ep.getMetadata().getName(), generatedLink.getSpec().getEntandoPluginName());
        assertEquals(ep.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoPluginNamespace());

        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getMetadata().getNamespace());
        assertEquals(
                String.format("%s-%s-link", ea.getMetadata().getName(), ep.getMetadata().getName()),
                generatedLink.getMetadata().getName());
    }

    @Test
    public void shouldDeleteEntandoAppPluginLink() {
        EntandoAppPluginLink el = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        linkService.delete(el);
        List<EntandoAppPluginLink> links = EntandoLinkTestHelper
                .getEntandoAppPluginLinkOperations(client)
                .inNamespace(el.getMetadata().getNamespace())
                .list().getItems();
        assertTrue(links.isEmpty());
    }

    @Test
    public void shouldNotThrowIfDeletingNonExistingPlugin() {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        Assertions.assertDoesNotThrow(() -> linkService.delete(el));
    }

}
