package org.entando.kubernetes.model;

import lombok.Data;
import org.entando.kubernetes.model.plugin.ExpectedRole;
import org.entando.kubernetes.model.plugin.Permission;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class EntandoPluginDeploymentRequest {

    private @NotEmpty String id;
    private @NotEmpty String namespace;
    private @NotEmpty String entandoAppName;
    private @NotEmpty String image;
    private @NotEmpty String ingressPath;
    private @NotEmpty String healthCheckPath;
    private @NotEmpty String dbms;

    private @Valid List<ExpectedRole> roles = new ArrayList<>();
    private @Valid List<Permission> permissions = new ArrayList<>();

}
