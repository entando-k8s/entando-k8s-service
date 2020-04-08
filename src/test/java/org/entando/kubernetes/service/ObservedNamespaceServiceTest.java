package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.util.MockObservedNamespaces;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class ObservedNamespaceServiceTest {

    private final String APP_NAMESPACE = "app-namespace";
    private final String PLUGIN_NAMESPACE = "plugin-namespace";
    private final String BUNDLE_NAMESPACE = "bundle-namespace";
    private final String NOT_OBSERVED_NAMESPACE = "an-external-namespace";

    ObservedNamespaces observedNamespaces;

    @BeforeEach
    public void setUp() {
        observedNamespaces = new MockObservedNamespaces(Arrays.asList(APP_NAMESPACE, PLUGIN_NAMESPACE, BUNDLE_NAMESPACE));
    }

    @Test
    public void shouldReturnObservedNamespaces() {
        List<String> nsNames = observedNamespaces.getNames();
        assertThat(nsNames).hasSize(4);
        assertThat(nsNames).doesNotContain(NOT_OBSERVED_NAMESPACE);
        assertThat(nsNames).containsExactlyInAnyOrder(PLUGIN_NAMESPACE,
                APP_NAMESPACE,
                BUNDLE_NAMESPACE,
                observedNamespaces.getCurrentNamespace()
        );

    }

    @Test
    public void shouldThrowAnExceptionIfNotObservedNamespace() {
        Assertions.assertThrows(NotObservedNamespaceException.class, () -> {
            observedNamespaces.failIfNotObserved(NOT_OBSERVED_NAMESPACE);
        });
    }

}
