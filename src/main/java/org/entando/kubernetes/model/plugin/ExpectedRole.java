package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import org.keycloak.representations.idm.RoleRepresentation;

@JsonDeserialize
public class ExpectedRole implements KubernetesResource {

    private String code;
    private String name;

    public ExpectedRole() {
        super();
    }

    public ExpectedRole(final String code) {
        this();
        this.setCode(code);
    }

    public ExpectedRole(String code, String name) {
        this(code);
        setName(name);
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
