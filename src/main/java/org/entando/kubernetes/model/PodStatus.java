package org.entando.kubernetes.model;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.PodCondition;
import java.util.List;
import lombok.Data;

@Data
public class PodStatus {

    private String phase;
    private List<PodCondition> conditions;
    private List<ContainerStatus> containerStatuses;

}
