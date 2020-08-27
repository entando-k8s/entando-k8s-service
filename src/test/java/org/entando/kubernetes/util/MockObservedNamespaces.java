package org.entando.kubernetes.util;

import java.util.Arrays;
import java.util.List;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.namespace.provider.StaticNamespaceProvider;
import org.entando.kubernetes.service.KubernetesUtils;

public class MockObservedNamespaces extends ObservedNamespaces {

    public MockObservedNamespaces() {
        super(new KubernetesUtils(new StaticNamespaceProvider("test-namespace")), null);
    }

    public MockObservedNamespaces(String... namespaces) {
        super(new KubernetesUtils(new StaticNamespaceProvider("test-namespace")), Arrays.asList(namespaces));
    }

    public MockObservedNamespaces(List<String> nsList) {
        super(new KubernetesUtils(new StaticNamespaceProvider("test-namespace")), nsList);
    }
}
