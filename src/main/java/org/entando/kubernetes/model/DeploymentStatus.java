package org.entando.kubernetes.model;

import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import lombok.Data;

import java.util.List;

@Data
public class DeploymentStatus {

    private int availableReplicas;
    private int readyReplicas;
    private int replicas;
    private int updatedReplicas;
    private List<DeploymentCondition> conditions;

}
