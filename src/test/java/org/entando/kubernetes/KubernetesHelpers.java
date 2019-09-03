package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;

public class KubernetesHelpers {

    public static DeploymentCondition createDeploymentCondition( String ts,  String message,
                                                        String reason,  String type) {
        DeploymentCondition condition = new DeploymentCondition();
        condition.setLastTransitionTime(ts);
        condition.setLastUpdateTime(ts);
        condition.setMessage(message);
        condition.setReason(reason);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }

    public static PodCondition createPodCondition( String ts,  String type) {
        PodCondition condition = new PodCondition();
        condition.setLastTransitionTime(ts);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }
}
