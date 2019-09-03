package org.entando.kubernetes.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.api.model.extensions.IngressStatus;

@JsonSerialize
@JsonDeserialize
public class JeeServerStatus extends AbstractServerStatus {
    private IngressStatus ingressStatus;

    public IngressStatus getIngressStatus() {
        return ingressStatus;
    }

    public void setIngressStatus(IngressStatus ingressStatus) {
        this.ingressStatus = ingressStatus;
    }
}
