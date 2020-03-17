package org.entando.kubernetes.exception;

public class NotObservedNamespaceException extends RuntimeException {

    private final String namespace;

    public NotObservedNamespaceException(String namespace) {
        super();
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
