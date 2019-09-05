package org.entando.kubernetes.model;

import io.fabric8.kubernetes.api.model.Doneable;

public interface DoneableEntandoCustomResource<D extends DoneableEntandoCustomResource, R extends EntandoCustomResource>
        extends Doneable<R> {

    D withStatus(AbstractServerStatus status);

    D withPhase(EntandoDeploymentPhase phase);
}
