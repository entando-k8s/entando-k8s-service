package org.entando.kubernetes.model.namespace.provider;

public class StaticNamespaceProvider implements NamespaceProvider {

    private final String namespace;

    public StaticNamespaceProvider(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }
}
