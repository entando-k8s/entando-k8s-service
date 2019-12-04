package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class EntandoPluginServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoPluginService entandoPluginService;

    private KubernetesClient client;

    @Before
    public void setUp() {
        client = server.getClient();
        entandoPluginService = new EntandoPluginService(client);
        EntandoPluginTestHelper.createEntandoPluginCrd(client);
    }

    @Test
    public void shouldReturnAnEmptyListIfNoPluginAvailable() {
        assertTrue(entandoPluginService.getAllPlugins().isEmpty());
    }

    @Test
    public void shouldReturnOnePlugin() throws IOException {
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin");
        assertEquals(1, entandoPluginService.getAllPlugins().size());
    }

    @Test
    public void shoudReturnPluginInClientNamespace() throws IOException {
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin");
        assertEquals(1, entandoPluginService.getAllPluginsInNamespace(client.getNamespace()).size());
    }

    @Test
    public void shouldFindAPluginInAnyNamespace() {
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin", "my-namespace");
        Optional<EntandoPlugin> opl = entandoPluginService.findPluginById("my-plugin");
        assertTrue(opl.isPresent());
        EntandoPlugin plg = opl.get();

        assertEquals("my-plugin", plg.getMetadata().getName());
        assertEquals("my-namespace", plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldFindAPluginInNamespace() {
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin", "my-namespace");
        Optional<EntandoPlugin> opl =
                entandoPluginService.findPluginByIdAndNamespace("my-plugin", "my-namespace");
        assertTrue(opl.isPresent());
        EntandoPlugin plg = opl.get();

        assertEquals("my-plugin", plg.getMetadata().getName());
        assertEquals("my-namespace", plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldNotFindPluginInNamespace() {
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin", "my-namespace");
        Optional<EntandoPlugin> opl =
                entandoPluginService.findPluginByIdAndNamespace("my-plugin", client.getNamespace());
        assertFalse(opl.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundPlugin() {
        assertFalse(entandoPluginService.findPluginById("some-plugin").isPresent());
    }

    @Test
    public void shouldDeployAPluginInProvidedNamespace() {
        EntandoPlugin testPlugin = EntandoPluginTestHelper.getTestEntandoPlugin("my-plugin");
        entandoPluginService.deploy(testPlugin);
        List<EntandoPlugin> availablePlugins = EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace(client.getNamespace()).list().getItems();
        assertEquals(1, availablePlugins.size());
        EntandoPlugin ep = availablePlugins.get(0);
        assertEquals("my-plugin", ep.getMetadata().getName());
        assertEquals(testPlugin.getSpec().getImage(), ep.getSpec().getImage());
        assertEquals(testPlugin.getSpec().getClusterInfrastructureTouse(), ep.getSpec().getClusterInfrastructureTouse());
        assertEquals(testPlugin.getSpec().getHealthCheckPath(), ep.getSpec().getHealthCheckPath());
        assertEquals(testPlugin.getSpec().getIngressPath(), ep.getSpec().getIngressPath());
        assertEquals(testPlugin.getSpec().getKeycloakSecretToUse(), ep.getSpec().getKeycloakSecretToUse());
        assertEquals(testPlugin.getSpec().getConnectionConfigNames(), ep.getSpec().getConnectionConfigNames());
        assertEquals(testPlugin.getSpec().getParameters(), ep.getSpec().getParameters());
        assertEquals(testPlugin.getSpec().getPermissions(), ep.getSpec().getPermissions());
        assertEquals(testPlugin.getSpec().getRoles(), ep.getSpec().getRoles());
        assertEquals(testPlugin.getSpec().getSecurityLevel(), ep.getSpec().getSecurityLevel());
        assertEquals(testPlugin.getSpec().getDbms(), ep.getSpec().getDbms());
        assertEquals(testPlugin.getSpec().getIngressHostName(), ep.getSpec().getIngressHostName());
        assertEquals(testPlugin.getSpec().getReplicas(), ep.getSpec().getReplicas());
        assertEquals(testPlugin.getSpec().getTlsSecretName(), ep.getSpec().getTlsSecretName());
    }

    @Test
    public void shouldDeletePluginInNamespace() {
        // given I have one plugin in the namespace
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin", "my-namespace");
        assertEquals(1, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems().size());
        // when I delete the plugin
        entandoPluginService.deletePluginInNamespace("my-plugin", "my-namespace");
        // That plugin is not available anymore
        assertEquals(0, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems().size());
    }

    @Test
    public void shouldDeletePluginAnywhere() {
        // given I have one plugin in the namespace
        EntandoPluginTestHelper.createEntandoPlugin(client, "my-plugin", "my-namespace");
        assertEquals(1, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems().size());
        // when I delete the plugin
        entandoPluginService.deletePlugin("my-plugin");
        // That plugin is not available anymore
        assertEquals(0, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems().size());
    }

}

