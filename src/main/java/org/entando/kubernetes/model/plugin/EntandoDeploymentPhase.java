package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.fabric8.zjsonpatch.internal.guava.Strings;

public enum EntandoDeploymentPhase {

    REQUESTED, STARTED, SUCCESSFUL, FAILED;

    @JsonCreator
    public static EntandoDeploymentPhase forValue(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return EntandoDeploymentPhase.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    public boolean requiresSync() {
        return this != STARTED;
    }
}
