package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoComponentBundleTestHelper.TEST_BUNDLE_NAMESPACE;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.util.EntandoComponentBundleTestHelper;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("component")
@EnableRuleMigrationSupport
public class EntandoComponentBundleServiceTest {

    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private EntandoComponentBundleService entandoDeBundleService;

    private KubernetesClient client;

    @BeforeEach
    public void setup() {
        client = server.getClient();
        ObservedNamespaces observedNamespaces = new MockObservedNamespaces(
                Collections.singletonList(TEST_BUNDLE_NAMESPACE)
        );
        entandoDeBundleService = new EntandoComponentBundleService(client,observedNamespaces);
        EntandoComponentBundleTestHelper.createEntandoComponentBundleCrd(client);
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
        EntandoComponentBundleTestHelper.createTestEntandoComponentBundle(client);
        assertThat(entandoDeBundleService.getAll().isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnBundlesAvailableInASpecificNamespace() {
        ObservedNamespaces ons = new MockObservedNamespaces(Arrays.asList("namespaceA", "namespaceB"));
        EntandoComponentBundleService customEntandoComponentBundleService = new EntandoComponentBundleService(client, ons);
        EntandoComponentBundleTestHelper.createTestEntandoComponentBundleInNamespace(client, "namespaceA");
        EntandoComponentBundleTestHelper.createTestEntandoComponentBundleInNamespace(client, "namespaceB");
        List<EntandoComponentBundle> bundlesInNamespaceB = customEntandoComponentBundleService.getAllInNamespace("namespaceB");
        assertThat(bundlesInNamespaceB.size()).isEqualTo(1);
        assertThat(bundlesInNamespaceB.get(0).getMetadata().getNamespace()).isEqualTo("namespaceB");
    }

    @Test
    public void shouldFindBundleByName() {
        EntandoComponentBundle bundle = EntandoComponentBundleTestHelper.createTestEntandoComponentBundle(client);
        String bundleName = bundle.getMetadata().getName();
        Optional<EntandoComponentBundle> foundBundles = entandoDeBundleService.findByName(bundleName);
        assertThat(foundBundles).isPresent();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldFindBundleByNameAndNamespace() {
        EntandoComponentBundle bundle = EntandoComponentBundleTestHelper.createTestEntandoComponentBundle(client);
        String bundleName = bundle.getMetadata().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        Optional<EntandoComponentBundle> foundBundles = entandoDeBundleService
                .findByNameAndNamespace(bundleName, bundleNamespace);
        assertThat(foundBundles).isNotEmpty();
        assertThat(foundBundles.get().getMetadata().getName()).isEqualTo(bundleName);
    }

    @Test
    public void shouldNotFindBundleByNameInWrongNamespace() {
        EntandoComponentBundle bundle = EntandoComponentBundleTestHelper.createTestEntandoComponentBundle(client);
        String bundleName = bundle.getMetadata().getName();
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            entandoDeBundleService.findByNameAndNamespace(bundleName, "myNamespace");
        });
    }
}
