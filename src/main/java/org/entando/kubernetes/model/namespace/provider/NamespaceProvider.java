package org.entando.kubernetes.model.namespace.provider;

public interface NamespaceProvider {
    String getNamespace();

    class NamespaceProviderException extends RuntimeException {

        public NamespaceProviderException(String message) {
            super(message);
        }


    }
}
