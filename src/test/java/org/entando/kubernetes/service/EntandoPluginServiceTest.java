package org.entando.kubernetes.service;

import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.zalando.problem.ThrowableProblem;

@Tag("component")
@EnableRuleMigrationSupport
public class EntandoPluginServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoPluginService entandoPluginService;

    private KubernetesClient client;

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        List<String> observedNamespaces = Arrays.asList(TEST_PLUGIN_NAMESPACE, "my-namespace");
        entandoPluginService = new EntandoPluginService(client, observedNamespaces);
        EntandoPluginTestHelper.createEntandoPluginCrd(client);
    }

    @Test
    public void shouldReturnAnEmptyListIfNoPluginAvailable() {
        assertTrue(entandoPluginService.getPlugins().isEmpty());
    }

    @Test
    public void shouldReturnOnePlugin() throws IOException {
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        assertEquals(1, entandoPluginService.getPlugins().size());
    }

    @Test
    public void shoudReturnPluginInClientNamespace() throws IOException {
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        assertEquals(1, entandoPluginService.getPluginsInNamespace(TEST_PLUGIN_NAMESPACE).size());
    }

    @Test
    public void shouldFindAPluginInAnyNamespace() {
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        Optional<EntandoPlugin> opl = entandoPluginService.findPluginById(TEST_PLUGIN_NAME);
        assertTrue(opl.isPresent());
        EntandoPlugin plg = opl.get();

        assertEquals(TEST_PLUGIN_NAME, plg.getMetadata().getName());
        assertEquals(TEST_PLUGIN_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldFindAPluginInNamespace() {
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        Optional<EntandoPlugin> opl =
                entandoPluginService.findPluginByIdAndNamespace(TEST_PLUGIN_NAME, TEST_PLUGIN_NAMESPACE);
        assertTrue(opl.isPresent());
        EntandoPlugin plg = opl.get();

        assertEquals(TEST_PLUGIN_NAME, plg.getMetadata().getName());
        assertEquals(TEST_PLUGIN_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldNotFindPluginInNamespace() {
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        Optional<EntandoPlugin> opl =
                entandoPluginService.findPluginByIdAndNamespace(TEST_PLUGIN_NAME, client.getNamespace());
        assertFalse(opl.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundPlugin() {
        assertFalse(entandoPluginService.findPluginById("some-plugin").isPresent());
    }

    @Test
    public void shouldDeployAPluginInProvidedNamespace() {
        EntandoPlugin testPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        entandoPluginService.deploy(testPlugin);
        List<EntandoPlugin> availablePlugins = EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace(TEST_PLUGIN_NAMESPACE).list().getItems();
        assertEquals(1, availablePlugins.size());
        EntandoPlugin ep = availablePlugins.get(0);
        assertEquals(TEST_PLUGIN_NAME, ep.getMetadata().getName());
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
    public void shouldDeployAPluginInAnotherObservedNamespace() {
        EntandoPlugin testPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        testPlugin.getMetadata().setNamespace("my-namespace");
        entandoPluginService.deploy(testPlugin);
        List<EntandoPlugin> availablePlugins = EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems();
        assertEquals(1, availablePlugins.size());
    }

    @Test
    public void shouldThrowAnExceptionWhenDeployingFromNotObservedNamespace() {
        EntandoPlugin testPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        testPlugin.getMetadata().setNamespace("not-observed-namespace");
        ThrowableProblem tp = Assertions.assertThrows(ThrowableProblem.class, () -> {
           entandoPluginService.deploy(testPlugin);
       });
       assertThat(tp.getMessage(), containsString(
               "Bad Request: Provided plugin " + testPlugin.getMetadata().getName() + " namespace " +
               testPlugin.getMetadata().getNamespace() + " is not observed by the service and therefore not usable"));
    }

    @Test
    public void shouldDeletePluginInNamespace() {
        // given I have one plugin in the namespace
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        assertEquals(1, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace(TEST_PLUGIN_NAMESPACE).list().getItems().size());
        // when I delete the plugin
        entandoPluginService.deletePluginInNamespace(TEST_PLUGIN_NAME, TEST_PLUGIN_NAMESPACE);
        // That plugin is not available anymore
        assertEquals(0, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace("my-namespace").list().getItems().size());
    }

    @Test
    public void shouldDeletePluginAnywhere() {
        // given I have one plugin in the namespace
        EntandoPluginTestHelper.createTestEntandoPlugin(client);
        assertEquals(1, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace(TEST_PLUGIN_NAMESPACE).list().getItems().size());
        // when I delete the plugin
        entandoPluginService.deletePlugin(TEST_PLUGIN_NAME);
        // That plugin is not available anymore
        assertEquals(0, EntandoPluginTestHelper.getEntandoPluginOperations(client)
                .inNamespace(TEST_PLUGIN_NAMESPACE).list().getItems().size());
    }

}

