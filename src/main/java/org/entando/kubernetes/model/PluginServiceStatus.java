package org.entando.kubernetes.model;

import lombok.Data;

@Data
public class PluginServiceStatus {

    private String type;
    private int replicas;
    private String volumePhase;
    private PodStatus podStatus;
    private DeploymentStatus deploymentStatus;

}
