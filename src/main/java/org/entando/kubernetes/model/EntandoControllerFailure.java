package org.entando.kubernetes.model;

import io.fabric8.kubernetes.api.model.KubernetesResource;

public class EntandoControllerFailure implements KubernetesResource {

    private String failedObjectType;
    private String failedObjectName;
    private String errorMessage;

    public String getFailedObjectType() {
        return failedObjectType;
    }

    public void setFailedObjectType(String failedObjectType) {
        this.failedObjectType = failedObjectType;
    }

    public String getFailedObjectName() {
        return failedObjectName;
    }

    public void setFailedObjectName(String failedObjectName) {
        this.failedObjectName = failedObjectName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
