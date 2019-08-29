package org.entando.kubernetes.model.plugin;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableEntandoPlugin extends CustomResourceDoneable<EntandoPlugin> implements DoneableEntandoCustomResource<DoneableEntandoPlugin, EntandoPlugin> {

    private final EntandoPlugin resource;

    public DoneableEntandoPlugin(final EntandoPlugin resource, final Function function) {
        super(resource, function);
        this.resource = resource;
    }

    public DoneableEntandoPlugin withStatus(AbstractServerStatus status) {
        if (status instanceof DbServerStatus) {
            this.resource.getStatus().addDbServerStatus((DbServerStatus) status);
        } else {
            this.resource.getStatus().addJeeServerStatus((JeeServerStatus) status);
        }
        return this;
    }

    @Override
    public DoneableEntandoPlugin withPhase(EntandoDeploymentPhase phase) {
        resource.getStatus().setEntandoDeploymentPhase(phase);
        return this;
    }
}
