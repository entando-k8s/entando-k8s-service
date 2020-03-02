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
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Tag;

@Tag("component")
public class EntandoAppServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoAppService entandoAppService;

    private KubernetesClient client;

    @Before
    public void setUp() {
        client = server.getClient();
        entandoAppService = new EntandoAppService(client, Collections.singletonList(TEST_APP_NAMESPACE));
        EntandoAppTestHelper.createEntandoAppCrd(client);
    }

    @Test
    public void shouldReturnAnEmptyListIfNoAppAvailable() {
        assertTrue(entandoAppService.getApps().isEmpty());
    }

    @Test
    public void shouldReturnOneApp() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getApps().size());
    }

    @Test
    public void shouldReturnAppInClientNamespace() throws IOException {
        EntandoAppTestHelper.createTestEntandoApp(client);
        assertEquals(1, entandoAppService.getAppsInNamespace(TEST_APP_NAMESPACE).size());
    }

    @Test
    public void shouldFindAnAppInAnyNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opApp = entandoAppService.findAppByName(TEST_APP_NAME);
        assertTrue(opApp.isPresent());
        EntandoApp app = opApp.get();

        assertEquals(TEST_APP_NAME, app.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, app.getMetadata().getNamespace());
    }

    @Test
    public void shouldFindAnAppInNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opl =
                entandoAppService.findAppByNameAndNamespace(TEST_APP_NAME, TEST_APP_NAMESPACE);
        assertTrue(opl.isPresent());
        EntandoApp plg = opl.get();

        assertEquals(TEST_APP_NAME, plg.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldNotFindAppInNamespace() {
        EntandoAppTestHelper.createTestEntandoApp(client);
        Optional<EntandoApp> opl =
                entandoAppService.findAppByNameAndNamespace(TEST_APP_NAME, client.getNamespace());
        assertFalse(opl.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundApp() {
        assertFalse(entandoAppService.findAppByName("some-App").isPresent());
    }

}
