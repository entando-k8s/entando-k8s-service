package org.entando.kubernetes.service;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.junit.Assert.assertEquals;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class EntandoLinkServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoLinkService linkService;

    private KubernetesClient client;

    @Before
    public void setUp() {
        client = server.getClient();
        linkService = new EntandoLinkService(client);
        EntandoLinkTestHelper.createEntandoAppPluginLinkCrd(client);
        EntandoAppTestHelper.createEntandoAppCrd(client);
        EntandoPluginTestHelper.createEntandoPluginCrd(client);
    }

    @Test
    public void shouldNotFindAnyLinkIfNoAppIsAvailable() {
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        assert(linkService.listAppLinks(testApp).isEmpty());
    }
    @Test
    public void shouldNotFindAnyLinkIfAppHasNoLink() {
        EntandoApp testApp = EntandoAppTestHelper.getTestEntandoApp();
        EntandoAppTestHelper.createTestEntandoApp(client);
        assert(linkService.listAppLinks(testApp).isEmpty());
    }

    @Test
    public void shouldFindLinkAssociatedWithApp() {
        EntandoApp testApp = EntandoAppTestHelper.createTestEntandoApp(client);
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.createTestEntandoAppPluginLink(client);
        assertEquals(1, linkService.listAppLinks(testApp).size());
    }

    @Test
    public void shouldCreateLinkBetweenAppAndPlugin() {
        EntandoApp testApp = EntandoAppTestHelper.createTestEntandoApp(client);
        EntandoPlugin testPlugin = EntandoPluginTestHelper.createTestEntandoPlugin(client);
        EntandoAppPluginLink testLink = EntandoLinkTestHelper.getTestLink();

        EntandoAppPluginLink createdLink = linkService.deploy(testLink);
        assertEquals(String.format("%s-to-%s-link", TEST_APP_NAME, TEST_PLUGIN_NAME), createdLink.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getMetadata().getNamespace());
        assertEquals(TEST_APP_NAME, createdLink.getSpec().getEntandoAppName());
        assertEquals(TEST_APP_NAMESPACE, createdLink.getSpec().getEntandoAppNamespace());
        assertEquals(TEST_PLUGIN_NAME, createdLink.getSpec().getEntandoPluginName());
        assertEquals(TEST_PLUGIN_NAMESPACE, createdLink.getSpec().getEntandoPluginNamespace());

        assertEquals(1, linkService.listEntandoAppLinks(TEST_APP_NAMESPACE, TEST_APP_NAME).size());
    }

    @Test
    public void shouldGenerateLinkWithAppropriateInfoFromAppAndPlugin() {
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();

        EntandoAppPluginLink generatedLink = linkService.generateForAppAndPlugin(ea, ep);
        assertEquals(ea.getMetadata().getName(), generatedLink.getSpec().getEntandoAppName());
        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoAppNamespace());
        assertEquals(ep.getMetadata().getName(), generatedLink.getSpec().getEntandoPluginName());
        assertEquals(ep.getMetadata().getNamespace(), generatedLink.getSpec().getEntandoPluginNamespace());

        assertEquals(ea.getMetadata().getNamespace(), generatedLink.getMetadata().getNamespace());
        assertEquals(
                String.format("%s-%s-link", ea.getMetadata().getName(), ep.getMetadata().getName()),
                generatedLink.getMetadata().getName());
    }


}
