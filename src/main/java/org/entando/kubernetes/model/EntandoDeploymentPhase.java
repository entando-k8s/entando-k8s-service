package org.entando.kubernetes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.fabric8.zjsonpatch.internal.guava.Strings;

public enum EntandoDeploymentPhase {
    REQUESTED, STARTED(), SUCCESSFUL(), FAILED();


    @JsonCreator
    public static EntandoDeploymentPhase forValue(String value) {
        return Strings.isNullOrEmpty(value) ? null : EntandoDeploymentPhase.valueOf(value.toUpperCase());
    }


    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    public boolean requiresSync() {
        return this != STARTED;
    }
}
