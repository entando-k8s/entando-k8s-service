package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import org.keycloak.representations.idm.RoleRepresentation;

@JsonDeserialize
public class ExpectedRole implements KubernetesResource {

    @JsonProperty
    private String code;
    @JsonProperty
    private String name;


    public ExpectedRole() {
        // Required for deserialization
    }


    public ExpectedRole(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RoleRepresentation toRepresentation() {
        final RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName(this.getCode());
        roleRepresentation.setDescription(this.getName());
        return roleRepresentation;
    }
}
