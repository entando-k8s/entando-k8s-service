package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.client.CustomResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter@Setter
@JsonSerialize
@JsonDeserialize
@NoArgsConstructor
@AllArgsConstructor
public class EntandoPlugin extends CustomResource implements EntandoCustomResource {

    @JsonProperty
    private EntandoPluginSpec spec;

    @JsonIgnore
    public EntandoCustomResourceStatus getStatus() {
        return spec.getEntandoStatus();
    }

    public void setStatus(EntandoCustomResourceStatus status) {
        this.spec.setEntandoStatus(status);
    }

}
