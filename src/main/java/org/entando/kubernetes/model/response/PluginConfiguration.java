package org.entando.kubernetes.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.List;
import lombok.Data;

@Data
public class PluginConfiguration {

    @JsonProperty("environment_variables")
    private List<EnvVar> environmentVariables;
}
