package org.entando.kubernetes.model;

import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import java.util.List;
import lombok.Data;

@Data
public class DeploymentStatus {

    private int availableReplicas;
    private int readyReplicas;
    private int replicas;
    private int updatedReplicas;
    private List<DeploymentCondition> conditions;

}
