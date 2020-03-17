package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
public class EntandoDeBundleServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoDeBundleService entandoDeBundleService;

    private KubernetesClient client;

    @BeforeEach
    public void setup() {
        client = server.getClient();
        entandoDeBundleService = new EntandoDeBundleService(client, Collections.singletonList(TEST_BUNDLE_NAMESPACE));
        EntandoDeBundleTestHelper.createEntandoDeBundleCrd(client);
    }

    @Test
    public void shouldStartCorrectly() {
        assertThat(entandoDeBundleService).isNotNull();
    }

    @Test
    public void shouldReturnEmptyListIfNoBundleIsAvailableInTheCluster() {
        assertThat(entandoDeBundleService.getBundles().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnBundlesAvailableInTheCluster() {
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        assertThat(entandoDeBundleService.getBundles().isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        EntandoDeBundle bundleA = EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceA");
        EntandoDeBundle bundleB = EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceB");
        List<EntandoDeBundle> bundlesInNamespaceB = entandoDeBundleService.getBundlesInNamespace("namespaceB");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
    }

    @Test
    public void shouldFindBundleByName() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundleByName(bundleName);
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getSpec().getDetails().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService
                .findBundleByNameAndNamespace(bundleName, bundleNamespace);
        assertThat(foundBundles).isNotEmpty();
        assertThat(foundBundles.get().getSpec().getDetails().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldNotFindBundleByNameInWrongNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        Optional<EntandoDeBundle> foundBundles = entandoDeBundleService
                .findBundleByNameAndNamespace(bundleName, "myNamespace");
        assertThat(foundBundles).isEmpty();
    }

    @Test
    public void shouldFindBundlesWithKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Collections.singletonList("entando6");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAnyKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    public void shouldFindBundlesWithAllKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    public void shouldNotFindBundleBecauseMissingAKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Arrays.asList("entando6", "my-custom-keyword");
        List<EntandoDeBundle> foundBundles = entandoDeBundleService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(0);
    }

}
