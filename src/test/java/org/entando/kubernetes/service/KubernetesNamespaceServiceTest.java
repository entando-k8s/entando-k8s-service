package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

@Tag("unit")
@EnableRuleMigrationSupport
public class KubernetesNamespaceServiceTest {
    @Rule
    public KubernetesServer server = new KubernetesServer(false, true);

    private KubernetesNamespaceService nsService;

    private KubernetesClient client;
    private final String APP_NAMESPACE = "app-namespace";
    private final String PLUGIN_NAMESPACE = "plugin-namespace";
    private final String BUNDLE_NAMESPACE = "bundle-namespace";
    private final String NOT_OBSERVED_NAMESPACE = "an-external-namespace";

    @BeforeEach
    public void setUp() {
        client = server.getClient();
        List<String> observedNamespaces = Arrays.asList(APP_NAMESPACE, PLUGIN_NAMESPACE, BUNDLE_NAMESPACE);
        nsService = new KubernetesNamespaceService(client, observedNamespaces);
        client.namespaces().createNew()
                .withNewMetadata()
                .withName(APP_NAMESPACE)
                .endMetadata().done();
        client.namespaces().createNew()
                .withNewMetadata()
                .withName(PLUGIN_NAMESPACE)
                .endMetadata().done();
        client.namespaces().createNew()
                .withNewMetadata()
                .withName(BUNDLE_NAMESPACE)
                .endMetadata().done();
        client.namespaces().createNew()
                .withNewMetadata()
                .withName(NOT_OBSERVED_NAMESPACE)
                .endMetadata().done();
    }

    @Test
    public void shouldReturnObservedNamespaces() {
        List<String> nsNames = nsService.getObservedNamespaceList().stream()
                .map(ns -> ns.getMetadata().getName())
                .collect(Collectors.toList());
        assertThat(nsNames).hasSize(3);
        assertThat(nsNames).doesNotContain(NOT_OBSERVED_NAMESPACE);
        assertThat(nsNames).containsExactlyInAnyOrder(PLUGIN_NAMESPACE, APP_NAMESPACE, BUNDLE_NAMESPACE);
    }

    @Test
    public void shouldReturnObservedNamespaceByName() {
       Optional<Namespace> obNs = nsService.getObservedNamespace(APP_NAMESPACE);
       assertThat(obNs.isPresent()).isTrue();
       assertThat(obNs.get().getMetadata().getName()).isEqualTo(APP_NAMESPACE);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAskingForNotObservedNamespace() {
        Optional<Namespace> obNs = nsService.getObservedNamespace(NOT_OBSERVED_NAMESPACE);
        assertThat(obNs.isPresent()).isFalse();
    }

}
