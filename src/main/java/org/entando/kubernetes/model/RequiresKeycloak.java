package org.entando.kubernetes.model;

public interface RequiresKeycloak extends EntandoCustomResource {

    String getKeycloakServerNamespace();

    String getKeycloakServerName();

}
