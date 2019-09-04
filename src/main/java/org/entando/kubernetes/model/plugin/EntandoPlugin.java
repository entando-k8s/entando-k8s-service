package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.client.CustomResource;
import org.entando.kubernetes.model.EntandoCustomResource;
import org.entando.kubernetes.model.EntandoCustomResourceStatus;
import org.entando.kubernetes.model.RequiresKeycloak;
import org.springframework.hateoas.core.Relation;

@JsonSerialize
@JsonDeserialize()
@Relation(collectionRelation = "plugins")
public class EntandoPlugin extends CustomResource implements EntandoCustomResource, RequiresKeycloak {
    @JsonProperty
    private EntandoPluginSpec spec;
    @JsonProperty
    private EntandoCustomResourceStatus entandoStatus;

    public EntandoPlugin() {
        setApiVersion("entando.org/v1alpha1");
    }

    public EntandoPlugin(EntandoPluginSpec spec) {
        this();
        this.spec = spec;
    }

    @JsonIgnore
    public EntandoPluginSpec getSpec() {
        return spec;
    }

    public void setSpec(EntandoPluginSpec spec) {
        this.spec = spec;
    }

    @JsonIgnore
    public EntandoCustomResourceStatus getStatus() {
        return entandoStatus == null ? entandoStatus = new EntandoCustomResourceStatus() : entandoStatus;
    }

    public void setStatus(EntandoCustomResourceStatus status) {
        this.entandoStatus = status;
    }

    @Override
    @JsonIgnore
    public String getKeycloakServerNamespace() {
        return spec.getKeycloakServerNamespace();
    }

    @Override
    @JsonIgnore
    public String getKeycloakServerName() {
        return spec.getKeycloakServerName();
    }
}
