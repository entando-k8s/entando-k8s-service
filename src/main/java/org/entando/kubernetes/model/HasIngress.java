package org.entando.kubernetes.model;

import java.util.Optional;

public interface HasIngress extends EntandoCustomResource {

    Optional<String> getIngressHostName();
    Optional<Boolean> getTlsEnabled();
}
