package org.entando.kubernetes.service;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
public class EntandoAppServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoAppService entandoAppService;

    private KubernetesClient client;

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        ObservedNamespaces ons = new MockObservedNamespaces(Collections.singletonList(TEST_APP_NAMESPACE));
        entandoAppService = new EntandoAppService(client, ons);
        EntandoAppTestHelper.createEntandoAppCrd(client);
    }

    @Test
    public void shouldReturnAnEmptyListIfNoAppAvailable() {
        assertTrue(entandoAppService.getAll().isEmpty());
    }

    @Test
    public void shouldReturnOneApp() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getAll().size());
    }

    @Test
    public void shouldReturnAppInClientNamespace() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getAllInNamespace(TEST_APP_NAMESPACE).size());
    }

    @Test
    public void shouldFindAnAppInAnyNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opApp = entandoAppService.findByName(TEST_APP_NAME);
        assertTrue(opApp.isPresent());
        EntandoApp app = opApp.get();

        assertEquals(TEST_APP_NAME, app.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, app.getMetadata().getNamespace());
    }

    @Test
    public void shouldFindAnAppInNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opl =
                entandoAppService.findByNameAndNamespace(TEST_APP_NAME, TEST_APP_NAMESPACE);
        assertTrue(opl.isPresent());
        EntandoApp plg = opl.get();

        assertEquals(TEST_APP_NAME, plg.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldThrowExceptionIfSearchingAppInNotObservedNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            entandoAppService.findByNameAndNamespace(TEST_APP_NAME, "any");
        });
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundApp() {
        assertFalse(entandoAppService.findByName("some-App").isPresent());
    }

}
