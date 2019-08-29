package org.entando.kubernetes.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class EntandoPluginDeployment {

    private String path;

    @NotEmpty
    private String version;

    @NotEmpty
    private String plugin;

    @NotNull
    private Map<String, String> envs;

}
