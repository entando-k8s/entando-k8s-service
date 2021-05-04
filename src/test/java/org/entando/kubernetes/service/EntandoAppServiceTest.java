package org.entando.kubernetes.service;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Optional;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.app.EntandoAppOperationFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
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
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoAppService entandoAppService;

    private KubernetesClient client;

    @BeforeEach
    void setUp() {
        client = server.getClient();
    }

    private void initalizeService(String... namespaces) {
        KubernetesUtils kubernetesUtils = new KubernetesUtils(token -> server.getClient().inNamespace(TEST_APP_NAMESPACE));
        kubernetesUtils.decode(KubernetesUtilsTest.K8S_TOKEN);
        ObservedNamespaces ons = new ObservedNamespaces(kubernetesUtils, Arrays.asList(namespaces), OperatorDeploymentType.HELM);
        entandoAppService = new EntandoAppService(kubernetesUtils, ons);
    }

    @Test
    void shouldReturnAnEmptyListIfNoAppAvailable() {
        initalizeService(TEST_APP_NAMESPACE);
        assertTrue(entandoAppService.getAll().isEmpty());
    }

    @Test
    void shouldReturnOneApp() {
        initalizeService(TEST_APP_NAMESPACE);
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        assertEquals(1, entandoAppService.getAll().size());
    }

    private void createTestEntandoApp(String testAppNamespace, String testAppName) {
        EntandoAppOperationFactory.produceAllEntandoApps(client).inNamespace(testAppNamespace).create(new EntandoAppBuilder()
                .withNewMetadata()
                .withName(testAppName)
                .withNamespace(testAppNamespace)
                .endMetadata()
                .build());
    }

    @Test
    void shouldReturnAppInClientNamespace() {
        initalizeService(TEST_APP_NAMESPACE);
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        assertEquals(1, entandoAppService.getAllInNamespace(TEST_APP_NAMESPACE).size());
    }

    @Test
    void shouldFindAnAppInAnyNamespaceClusteredScope() {
        initalizeService("*");
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        createTestEntandoApp("namespace2", "app2");
        Optional<EntandoApp> foundApp1 = entandoAppService.findByName(TEST_APP_NAME);
        assertTrue(foundApp1.isPresent());
        EntandoApp app1 = foundApp1.get();

        assertEquals(TEST_APP_NAME, app1.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, app1.getMetadata().getNamespace());
        Optional<EntandoApp> foundApp2 = entandoAppService.findByName("app2");
        assertTrue(foundApp2.isPresent());
        EntandoApp app2 = foundApp2.get();

        assertEquals("app2", app2.getMetadata().getName());
        assertEquals("namespace2", app2.getMetadata().getNamespace());
    }

    @Test
    void shouldFindAnAppInAnyNamespace() {
        initalizeService(TEST_APP_NAMESPACE, "namespace2");
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        createTestEntandoApp("namespace2", "app2");
        Optional<EntandoApp> foundApp1 = entandoAppService.findByName(TEST_APP_NAME);
        assertTrue(foundApp1.isPresent());
        EntandoApp app1 = foundApp1.get();

        assertEquals(TEST_APP_NAME, app1.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, app1.getMetadata().getNamespace());
        Optional<EntandoApp> foundApp2 = entandoAppService.findByName("app2");
        assertTrue(foundApp2.isPresent());
        EntandoApp app2 = foundApp2.get();

        assertEquals("app2", app2.getMetadata().getName());
        assertEquals("namespace2", app2.getMetadata().getNamespace());
    }

    @Test
    void shouldFindAnAppInNamespace() {
        initalizeService(TEST_APP_NAMESPACE);
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        Optional<EntandoApp> opl =
                entandoAppService.findByNameAndNamespace(TEST_APP_NAME, TEST_APP_NAMESPACE);
        assertTrue(opl.isPresent());
        EntandoApp plg = opl.get();

        assertEquals(TEST_APP_NAME, plg.getMetadata().getName());
        assertEquals(TEST_APP_NAMESPACE, plg.getMetadata().getNamespace());
    }

    @Test
    void shouldThrowExceptionIfSearchingAppInNotObservedNamespace() {
        initalizeService(TEST_APP_NAMESPACE);
        createTestEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME);
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            entandoAppService.findByNameAndNamespace(TEST_APP_NAME, "any");
        });
    }

    @Test
    void shouldReturnEmptyOptionalForNotFoundApp() {
        initalizeService(TEST_APP_NAMESPACE);
        assertFalse(entandoAppService.findByName("some-App").isPresent());
    }

}
