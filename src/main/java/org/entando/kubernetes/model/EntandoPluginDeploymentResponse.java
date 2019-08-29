package org.entando.kubernetes.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;
import java.util.Map;

@Getter@Setter
public class EntandoPluginDeploymentResponse extends ResourceSupport {

    private String plugin;
    private String image;
    private String path;
    private int replicas;
    private boolean online;
    private String deploymentPhase;

    private PluginServiceStatus serverStatus;
    private List<PluginServiceStatus> externalServiceStatuses;

    private String digitalExchangeId;
    private String digitalExchangeUrl;

    /**
     * Those must only be the configurable ones
     */
    private Map<String, String> envVariables;

}
