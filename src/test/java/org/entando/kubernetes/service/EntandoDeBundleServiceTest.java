package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.getTestEntandoDeBundle;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.security.oauth2.KubernetesUtilsTest;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tags({@Tag("component"), @Tag("in-process")})
@EnableRuleMigrationSupport
@EnableKubernetesMockClient(crud = true, https = false)
class EntandoDeBundleServiceTest {

    private EntandoDeBundleService entandoDeBundleService;

    static KubernetesClient client;

    @BeforeEach
    public void setup() {
        EntandoDeBundleTestHelper.deleteAllEntandoDeBundleInNamespace(client, TEST_BUNDLE_NAMESPACE);
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
        assertThat(entandoDeBundleService.getAll().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnBundlesAvailableInTheObservedNamespace() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        assertThat(entandoDeBundleService.getAll().isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        initializeService("namespaceA", "namespaceB");
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceA");
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceB");
        List<EntandoDeBundle> bundlesInNamespaceB = entandoDeBundleService.getAllInNamespace("namespaceB");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
    }

    @Test
    public void shouldFindBundleByName() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getMetadata().getName();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService.findByName(bundleName);
        assertThat(foundBundles).isPresent();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getMetadata().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService
                .findByNameAndNamespace(bundleName, bundleNamespace);
        assertThat(foundBundles).isNotEmpty();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    void shouldFindBundlesWithKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Collections.singletonList("entando6");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAnyKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldFindBundlesWithAllKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldNotFindBundleBecauseMissingAKeywords() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Arrays.asList("entando6", "my-custom-keyword");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).isEmpty();
    }

    @Test
    void shouldCreateBundle() {
        initializeService(TEST_BUNDLE_NAMESPACE);
        EntandoDeBundle bundle = getTestEntandoDeBundle();

        entandoDeBundleService.createBundle(bundle);

        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }
}
