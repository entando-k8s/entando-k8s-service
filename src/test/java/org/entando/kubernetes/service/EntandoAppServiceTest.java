package org.entando.kubernetes.service;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.util.CustomKubernetesServer;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tags({@Tag("component"), @Tag("in-process")})
@EnableRuleMigrationSupport
class EntandoAppServiceTest {

    @Rule
    public CustomKubernetesServer server = new CustomKubernetesServer(true, true);

    private EntandoAppService entandoAppService;

    private KubernetesClient client;

    @BeforeEach
    void setUp() {
        client = server.getClient();
        ObservedNamespaces ons = new MockObservedNamespaces(Collections.singletonList(TEST_APP_NAMESPACE));
        entandoAppService = new EntandoAppService(client, ons);
    }

    @Test
    void shouldReturnAnEmptyListIfNoAppAvailable() {
        assertTrue(entandoAppService.getAll().isEmpty());
    }

    @Test
    void shouldReturnOneApp() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getAll().size());
    }

    @Test
    void shouldReturnAppInClientNamespace() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getAllInNamespace(TEST_APP_NAMESPACE).size());
    }

    @Test
    void shouldFindAnAppInAnyNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opApp = entandoAppService.findByName(TEST_APP_NAME);
        assertTrue(opApp.isPresent());
        EntandoApp app = opApp.get();

        assertEquals(TEST_APP_NAME, app.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, app.getMetadata().getNamespace());
    }

    @Test
    void shouldFindAnAppInNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opl =
                entandoAppService.findByNameAndNamespace(TEST_APP_NAME, TEST_APP_NAMESPACE);
        assertTrue(opl.isPresent());
        EntandoApp plg = opl.get();

        assertEquals(TEST_APP_NAME, plg.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    void shouldThrowExceptionIfSearchingAppInNotObservedNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            entandoAppService.findByNameAndNamespace(TEST_APP_NAME, "any");
        });
    }

    @Test
    void shouldReturnEmptyOptionalForNotFoundApp() {
        assertFalse(entandoAppService.findByName("some-App").isPresent());
    }

}
