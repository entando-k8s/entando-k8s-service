package org.entando.kubernetes.model;

import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

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
