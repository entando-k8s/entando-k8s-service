package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAME;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.getTestEntandoDeBundleSpec;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleBuilder;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.zalando.problem.ThrowableProblem;

@Tags({@Tag("component"), @Tag("in-process")})
@EnableRuleMigrationSupport
@EnableKubernetesMockClient(crud = true, https = false)
class EntandoDeBundleServiceTest {

    private EntandoDeBundleService entandoDeBundleService;

    static KubernetesClient client;

    @BeforeEach
    public void setup() {
        EntandoDeBundleTestHelper.deleteAllEntandoDeBundleInNamespace(client, TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.deleteAllEntandoDeBundleInNamespace(client, "test");
    }

    private void initializeService(String... namespaces) {
        KubernetesUtils kubernetesUtils = new KubernetesUtils(token -> client);
        kubernetesUtils.decode(KubernetesUtilsTest.NON_K8S_TOKEN);
        ObservedNamespaces ons = new ObservedNamespaces(kubernetesUtils, Arrays.asList(namespaces),
                OperatorDeploymentType.HELM);
        entandoDeBundleService = new EntandoDeBundleService(kubernetesUtils, ons);
    }

    @Test
    public void shouldReturnEmptyListIfNoBundleIsAvailableInTheObservedNamespace() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        assertThat(entandoDeBundleService.getAll()).isEmpty();
        assertThat(entandoDeBundleService.getAll("primary")).isEmpty();
    }

    @Test
    public void shouldReturnBundlesAvailableInTheObservedNamespace() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client, "tenant1");
        assertThat(entandoDeBundleService.getAll("tenant1")).isNotEmpty();
        assertThat(entandoDeBundleService.getAll("tenant45")).isEmpty();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        initializeService("namespaceA", "namespaceB");
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceA", "tenant1");
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceB", "primary");
        List<EntandoDeBundle> bundlesInNamespaceB = entandoDeBundleService.getAllInNamespace("namespaceB", "primary");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getAnnotations()).containsEntry("entando.org/tenants", "primary");
    }

    @Test
    public void shouldFindBundleByName() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client, "primary");
        String bundleName = bundle.getMetadata().getName();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService.findByNameAndNamespace(bundleName, TEST_BUNDLE_NAMESPACE, "primary");
        assertThat(foundBundles).isPresent();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle1 = EntandoDeBundleTestHelper.getTestEntandoDeBundle("primary");
        bundle1.getMetadata().setNamespace("test");

        entandoDeBundleService.createBundle(bundle1, "primary");
        entandoDeBundleService.createBundle(bundle1, "tenant1");

        String bundleName = bundle1.getMetadata().getName();
        String bundleNamespace = bundle1.getMetadata().getNamespace();

        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService
                .findByNameAndNamespace(bundleName, bundleNamespace, "primary");
        assertThat(foundBundles).isNotEmpty();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
        assertThat(foundBundles.get().getMetadata().getAnnotations().get("entando.org/tenants")).contains("primary", "tenant1");

    }

    @Test
    void shouldFindBundlesWithKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client, "primary");
        List<String> bundleKeywords = Collections.singletonList("entando6");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAnyKeywords(bundleKeywords, "primary");
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldFindBundlesWithAllKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client, "primary");
        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords, "primary");
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldNotFindBundleBecauseMissingAKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client, "primary");
        List<String> bundleKeywords = Arrays.asList("entando6", "my-custom-keyword");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords, "primary");
        assertThat(foundBundles).isEmpty();
    }

    @Test
    void shouldCreateBundle() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("tenant2");
        bundle.getMetadata().setNamespace("test");

        entandoDeBundleService.createBundle(bundle, "tenant2");

        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords, "tenant2");
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getMetadata().getAnnotations().get("entando.org/tenants")).contains("tenant2");
    }

    @Test
    void shouldCreateAlreadyExistingBundleForNotPrimaryTenant() {
        initializeService(TEST_BUNDLE_NAMESPACE);

        EntandoDeBundle bundleWithoutTenantAnnotation = EntandoDeBundleTestHelper.getTestEntandoDeBundle("");
        bundleWithoutTenantAnnotation.getMetadata().setAnnotations(null);
        bundleWithoutTenantAnnotation.getMetadata().setNamespace("test");
        EntandoDeBundleService.getBundleOperations(client).inNamespace("test").create(bundleWithoutTenantAnnotation);

        EntandoDeBundle bundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("tenant2");
        bundle.getMetadata().setNamespace("test");

        entandoDeBundleService.createBundle(bundle, "tenant2");

        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords, "tenant2");
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getMetadata().getAnnotations().get("entando.org/tenants")).contains("primary",
                "tenant2");
    }

    @Test
    void shouldCreateBundleAlsoIfAnnotationsIsMissing() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("tenant2");
        bundle.getMetadata().setAnnotations(null);
        bundle.getMetadata().setNamespace("test");

        entandoDeBundleService.createBundle(bundle, "tenant2");

        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords, "tenant2");
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getMetadata().getAnnotations().get("entando.org/tenants")).contains("tenant2");
    }

    @Test
    void givenThatABundleExistsShouldDeleteIt() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        assertThat(entandoDeBundleService.getAll("primary")).isEmpty();

        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "test", "primary");
        assertThat(entandoDeBundleService.getAll("primary")).hasSize(1);

        entandoDeBundleService.deleteBundle(EntandoDeBundleTestHelper.TEST_BUNDLE_NAME, "primary");
        assertThat(entandoDeBundleService.getAll("primary")).isEmpty();
    }

    @Test
    void givenThatABundleDoesNotExistsShouldThrowNotFoundException() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        assertThat(entandoDeBundleService.getAll("primary")).isEmpty();

        assertThatExceptionOfType(ThrowableProblem.class)
                .isThrownBy(() -> entandoDeBundleService.deleteBundle(EntandoDeBundleTestHelper.TEST_BUNDLE_NAME, "primary"))
                .withMessage("Not Found: Bundle with name " + EntandoDeBundleTestHelper.TEST_BUNDLE_NAME + " not found in observed namespace");
    }
}
