package org.entando.kubernetes.model;

import javax.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class EnvironmentVariable {

    @NotEmpty
    private String id;

    @NotEmpty
    private String description;
    private String defaultValue;
    private boolean optional;

}
