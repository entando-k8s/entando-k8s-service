package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonSerialize
@JsonDeserialize
public class EntandoCustomResourceStatus {

    private List<DbServerStatus> dbServerStatus = new ArrayList<>();
    private List<JeeServerStatus> jeeServerStatus = new ArrayList<>();

    @JsonProperty(defaultValue = "requested")
    private EntandoDeploymentPhase entandoDeploymentPhase = EntandoDeploymentPhase.REQUESTED;

    public EntandoCustomResourceStatus() {
    }

    public EntandoDeploymentPhase getEntandoDeploymentPhase() {
        return entandoDeploymentPhase;
    }

    public void setEntandoDeploymentPhase(EntandoDeploymentPhase entandoDeploymentPhase) {
        this.entandoDeploymentPhase = entandoDeploymentPhase;
    }

    public boolean hasFailed() {
        return dbServerStatus.stream().anyMatch(s -> s.hasFailed()) || jeeServerStatus.stream().anyMatch(s -> s.hasFailed());
    }

    public void addJeeServerStatus(JeeServerStatus status) {
        jeeServerStatus.add(status);
    }

    public void addDbServerStatus(DbServerStatus status) {
        dbServerStatus.add(status);
    }

    public List<DbServerStatus> getDbServerStatus() {
        return dbServerStatus;
    }

    public List<JeeServerStatus> getJeeServerStatus() {
        return jeeServerStatus;
    }

    public Optional<DbServerStatus> forDbQualifiedBy(String qualifier) {
        return getDbServerStatus().stream().filter(s -> s.getQualifier().equals(qualifier)).findFirst();
    }

    public Optional<JeeServerStatus> forServerQualifiedBy(String qualifier) {
        return getJeeServerStatus().stream().filter(s -> s.getQualifier().equals(qualifier)).findFirst();
    }

    public EntandoDeploymentPhase calculateFinalPhase() {
        return hasFailed() ? EntandoDeploymentPhase.FAILED : EntandoDeploymentPhase.SUCCESSFUL;
    }

}
