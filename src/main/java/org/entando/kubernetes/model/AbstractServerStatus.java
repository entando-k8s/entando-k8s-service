package org.entando.kubernetes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimStatus;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.ServiceStatus;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;

public class AbstractServerStatus implements KubernetesResource {

    private String qualifier;
    private ServiceStatus serviceStatus;
    private DeploymentStatus deploymentStatus;
    private PodStatus podStatus;
    private EntandoControllerFailure entandoControllerFailure;
    private PersistentVolumeClaimStatus persistentVolumeClaimStatus;

    @JsonProperty
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    @JsonProperty
    public DeploymentStatus getDeploymentStatus() {
        return deploymentStatus;
    }

    public void setDeploymentStatus(DeploymentStatus deploymentStatus) {
        this.deploymentStatus = deploymentStatus;
    }

    @JsonProperty
    public PodStatus getPodStatus() {
        return podStatus;
    }

    public void setPodStatus(PodStatus podStatus) {
        this.podStatus = podStatus;
    }

    @JsonProperty
    public EntandoControllerFailure getEntandoControllerFailure() {
        return entandoControllerFailure;
    }

    public void setEntandoControllerFailure(EntandoControllerFailure entandoControllerFailure) {
        this.entandoControllerFailure = entandoControllerFailure;
    }

    @JsonIgnore
    public boolean hasFailed() {
        return entandoControllerFailure != null;
    }

    public void setPersistentVolumeClaimStatus(PersistentVolumeClaimStatus persistentVolumeClaimStatus) {
        this.persistentVolumeClaimStatus = persistentVolumeClaimStatus;
    }

    @JsonProperty
    public PersistentVolumeClaimStatus getPersistentVolumeClaimStatus() {
        return persistentVolumeClaimStatus;
    }

    @JsonProperty
    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }
}
