package org.entando.kubernetes.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class EnvironmentVariable {

    @NotEmpty
    private String id;

    @NotEmpty
    private String description;
    private String defaultValue;
    private boolean optional;

}
