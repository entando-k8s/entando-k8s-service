package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize
public class Permission implements KubernetesResource {

    private String clientId;
    private String role;

    public Permission() {
        // Required for deserialization
    }

    public Permission(final String clientId, final String role) {
        this.clientId = clientId;
        this.role = role;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
