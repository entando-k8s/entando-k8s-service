package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ObservedNamespaceServiceTest {

    private static final String APP_NAMESPACE = "app-namespace";
    private static final String PLUGIN_NAMESPACE = "plugin-namespace";
    private static final String BUNDLE_NAMESPACE = "bundle-namespace";
    private static final String NOT_OBSERVED_NAMESPACE = "an-external-namespace";

    ObservedNamespaces observedNamespaces;

    @BeforeEach
    public void setUp() {
        observedNamespaces = new ObservedNamespaces(new KubernetesUtils(null) {
            @Override
            public String getCurrentNamespace() {
                return "test-namespace";
            }
        }, Arrays.asList(APP_NAMESPACE, PLUGIN_NAMESPACE, BUNDLE_NAMESPACE), OperatorDeploymentType.HELM);
    }

    @Test
    void shouldReturnObservedNamespaces() {
        List<String> nsNames = observedNamespaces.getNames();
        assertThat(nsNames).hasSize(4);
        assertThat(nsNames).doesNotContain(NOT_OBSERVED_NAMESPACE);
        assertThat(nsNames).containsExactlyInAnyOrder(PLUGIN_NAMESPACE,
                APP_NAMESPACE,
                BUNDLE_NAMESPACE,
                observedNamespaces.getCurrentNamespace()
        );

    }


}
