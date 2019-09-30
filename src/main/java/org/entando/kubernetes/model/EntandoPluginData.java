package org.entando.kubernetes.model;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EntandoPluginData {

    @Pattern(regexp = "[a-z-]+")
    private String id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String image;

    @NotEmpty
    private String version;

    @NotNull
    private int port;

    @NotNull
    @Valid
    private List<EnvironmentVariable> envVariables;

}
