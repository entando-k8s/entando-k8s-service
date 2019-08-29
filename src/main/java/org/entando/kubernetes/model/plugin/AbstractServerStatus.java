package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimStatus;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.ServiceStatus;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import lombok.Data;

@Data
@JsonSerialize
@JsonDeserialize
public class AbstractServerStatus implements KubernetesResource {

    private String qualifier;
    private ServiceStatus serviceStatus;
    private DeploymentStatus deploymentStatus;
    private PodStatus podStatus;
    private EntandoControllerFailure entandoControllerFailure;
    private PersistentVolumeClaimStatus persistentVolumeClaimStatus;

    @JsonIgnore
    public boolean hasFailed() {
        return entandoControllerFailure != null;
    }

}
