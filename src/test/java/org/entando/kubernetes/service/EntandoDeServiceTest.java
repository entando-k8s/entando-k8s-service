package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleBuilder;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class EntandoDeServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoDeService entandoDeService;

    private KubernetesClient client;

    @Before
    public void setup() {
        client = server.getClient();
        entandoDeService = new EntandoDeService(client);
        EntandoDeBundleTestHelper.createEntandoDeBundleCrd(client);
    }

    @Test
    public void shouldStartCorrectly() {
        assertThat(entandoDeService).isNotNull();
    }

    @Test
    public void shouldReturnEmptyListIfNoBundleIsAvailableInTheCluster() {
        assertThat(entandoDeService.getAllBundles().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnBundlesAvailableInTheCluster() {
        EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        assertThat(entandoDeService.getAllBundles().isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        EntandoDeBundle bundleA = EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceA");
        EntandoDeBundle bundleB = EntandoDeBundleTestHelper.createTestEntandoDeBundleInNamespace(client, "namespaceB");
        List<EntandoDeBundle> bundlesInNamespaceB = entandoDeService.getAllBundlesInNamespace("namespaceB");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
    }

    @Test
    public void shouldFindBundleByName() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByName(bundleName);
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getSpec().getDetails().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByNameAndNamespace(bundleName, bundleNamespace);
        assertThat(foundBundles).hasSize(1);
        assertThat(foundBundles.get(0).getSpec().getDetails().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldNotFindBundleByNameInWrongNamespace() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        String bundleName = bundle.getSpec().getDetails().getName();
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByNameAndNamespace(bundleName, "myNamespace");
        assertThat(foundBundles).hasSize(0);
    }

    @Test
    public void shouldFindBundlesWithKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Collections.singletonList("entando6");
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByAnyKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    public void shouldFindBundlesWithAllKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = bundle.getSpec().getDetails().getKeywords();
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(1);
    }

    @Test
    public void shouldNotFindBundleBecauseMissingAKeywords() {
        EntandoDeBundle bundle = EntandoDeBundleTestHelper.createTestEntandoDeBundle(client);
        List<String> bundleKeywords = Arrays.asList("entando6", "my-custom-keyword");
        List<EntandoDeBundle> foundBundles = entandoDeService.findBundlesByAllKeywords(bundleKeywords);
        assertThat(foundBundles).hasSize(0);
    }

}
