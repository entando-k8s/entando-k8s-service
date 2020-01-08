package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
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
}
