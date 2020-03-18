package org.entando.kubernetes.exception;

import org.zalando.problem.AbstractThrowableProblem;

public class NotObservedNamespaceException extends AbstractThrowableProblem {

    private final String namespace;

    public NotObservedNamespaceException(String namespace) {
        super();
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
