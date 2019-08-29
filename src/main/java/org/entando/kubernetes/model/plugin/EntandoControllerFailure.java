package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import lombok.Data;

@Data
@JsonSerialize
@JsonDeserialize
public class EntandoControllerFailure implements KubernetesResource {

    private String failedObjectType;
    private String failedObjectName;
    private String errorMessage;

}
