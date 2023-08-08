package org.entando.kubernetes.model.response;

import io.fabric8.kubernetes.api.model.EnvVar;
import java.util.List;
import lombok.Data;

@Data
public class PluginConfiguration {

    private List<EnvVar> environmentVariables;
}
