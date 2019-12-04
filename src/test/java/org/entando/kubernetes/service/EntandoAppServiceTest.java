package org.entando.kubernetes.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class EntandoAppServiceTest {
    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoAppService entandoAppService;

    private KubernetesClient client;

    @Before
    public void setUp() {
        client = server.getClient();
        entandoAppService = new EntandoAppService(client);
        EntandoAppTestHelper.createEntandoAppCrd(client);
    }

    @Test
    public void shouldReturnAnEmptyListIfNoAppAvailable() {
        assertTrue(entandoAppService.listEntandoApps().isEmpty());
    }

    @Test
    public void shouldReturnOneApp() throws IOException {
        EntandoAppTestHelper.createEntandoApp(client, "my-App");
        assertEquals(1, entandoAppService.listEntandoApps().size());
    }

    @Test
    public void shoudReturnAppInClientNamespace() throws IOException {
        EntandoAppTestHelper.createEntandoApp(client, "my-App");
        assertEquals(1, entandoAppService.listEntandoAppsInNamespace(client.getNamespace()).size());
    }

    @Test
    public void shouldFindAAppInAnyNamespace() {
        EntandoAppTestHelper.createEntandoApp(client, "my-App", "my-namespace");
        Optional<EntandoApp> opApp = entandoAppService.findAppByName("my-App");
        assertTrue(opApp.isPresent());
        EntandoApp app = opApp.get();

        assertEquals("my-App", app.getMetadata().getName());
        assertEquals("my-namespace", app.getMetadata().getNamespace());
    }

    @Test
    public void shouldFindAAppInNamespace() {
        EntandoAppTestHelper.createEntandoApp(client, "my-App", "my-namespace");
        Optional<EntandoApp> opl =
                entandoAppService.findAppByNameAndNamespace("my-App", "my-namespace");
        assertTrue(opl.isPresent());
        EntandoApp plg = opl.get();

        assertEquals("my-App", plg.getMetadata().getName());
        assertEquals("my-namespace", plg.getMetadata().getNamespace());
    }

    @Test
    public void shouldNotFindAppInNamespace() {
        EntandoAppTestHelper.createEntandoApp(client, "my-App", "my-namespace");
        Optional<EntandoApp> opl =
                entandoAppService.findAppByNameAndNamespace("my-App", client.getNamespace());
        assertFalse(opl.isPresent());
    }

    @Test
    public void shouldReturnEmptyOptionalForNotFoundApp() {
        assertFalse(entandoAppService.findAppByName("some-App").isPresent());
    }

}
