package org.entando.kubernetes.model.plugin;

import io.fabric8.kubernetes.api.model.extensions.IngressStatus;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class JeeServerStatus extends AbstractServerStatus {

    private IngressStatus ingressStatus;

}