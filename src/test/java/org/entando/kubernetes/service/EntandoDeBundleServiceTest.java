package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;

import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleOperationFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.util.CustomKubernetesServer;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
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
class EntandoDeBundleServiceTest {

    @Rule
    public CustomKubernetesServer server = new CustomKubernetesServer(false, true);

    private EntandoDeBundleService entandoDeBundleService;

    private KubernetesClient client;

    @BeforeEach
    public void setup() {
        client = server.getClient();
        ObservedNamespaces observedNamespaces = new MockObservedNamespaces(
                Collections.singletonList(TEST_BUNDLE_NAMESPACE)
        );
        entandoDeBundleService = new EntandoDeBundleService(client,observedNamespaces);
        EntandoDeBundleOperationFactory.produceAllEntandoDeBundles(server.getClient());
    }

    @Test
    public void shouldStartCorrectly() {
        assertThat(entandoDeBundleService).isNotNull();
    }

    @Test
    public void shouldReturnEmptyListIfNoBundleIsAvailableInTheObservedNamespace() {
        assertThat(entandoDeBundleService.getAll().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnBundlesAvailableInTheObservedNamespace() {
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        assertThat(entandoDeBundleService.getAll().isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        ObservedNamespaces ons = new MockObservedNamespaces(Arrays.asList("namespaceA", "namespaceB"));
        EntandoDeBundleService customEntandoDeBundleService = new EntandoDeBundleService(client, ons);
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceA");
        EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceB");
        List<EntandoDeBundle> bundlesInNamespaceB = customEntandoDeBundleService.getAllInNamespace("namespaceB");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
    }

    @Test
    public void shouldFindBundleByName() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getMetadata().getName();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService.findByName(bundleName);
        assertThat(foundBundles).isPresent();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getMetadata().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService
                .findByNameAndNamespace(bundleName, bundleNamespace);
        assertThat(foundBundles).isNotEmpty();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    void shouldNotFindBundleByNameInWrongNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            entandoDeBundleService.findByNameAndNamespace(bundleName, "myNamespace");
        });
    }

    @Test
    void shouldFindBundlesWithKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Collections.singletonList("entando6");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAnyKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldFindBundlesWithAllKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    void shouldNotFindBundleBecauseMissingAKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Arrays.asList("entando6", "my-custom-keyword");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).isEmpty();
    }

}
