package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@JsonSerialize
@JsonDeserialize
public class EntandoPluginSpec implements KubernetesResource {

    private String entandoAppName;
    private String image;
    private int replicas = 1;
    private String dbms;
    private String ingressPath;
    private String healthCheckPath;
    private EntandoCustomResourceStatus entandoStatus;

    @JsonSerialize
    @JsonDeserialize
    private List<ExpectedRole> roles = new ArrayList<>();

    @JsonSerialize
    @JsonDeserialize
    private List<Permission> permissions = new ArrayList<>();

    @JsonSerialize
    @JsonDeserialize
    private Map<String, Object> parameters = new HashMap<>();

}
