package org.entando.kubernetes.model.plugin;

import io.fabric8.kubernetes.api.model.HasMetadata;

public interface EntandoCustomResource extends HasMetadata {
    EntandoCustomResourceStatus getStatus();

    void setStatus(EntandoCustomResourceStatus status);
}
