package org.entando.kubernetes.util;

import java.util.List;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.model.ObservedNamespaces;

public class MockObservedNamespaces extends ObservedNamespaces {

    public MockObservedNamespaces() {
        super(new TestKubernetesConfig().k8sUtils(), null);
    }

    public MockObservedNamespaces(List<String> nsList) {
        super(new TestKubernetesConfig().k8sUtils(), nsList);
    }
}
