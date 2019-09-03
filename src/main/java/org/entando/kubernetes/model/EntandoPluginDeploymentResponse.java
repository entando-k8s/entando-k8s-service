package org.entando.kubernetes.model;

import lombok.Getter;
import lombok.Setter;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;
import java.util.Map;

@Getter@Setter
public class EntandoPluginDeploymentResponse extends ResourceSupport {

    private EntandoPlugin entandoPlugin;
//    private String plugin;
//    private String namespace;
//    private String image;
//    private String path;
//    private int replicas;
//    private boolean online;
//    private String deploymentPhase;
//
//    private EntandoCustomResourceStatus status;
//
//    /**
//     * Those must only be the configurable ones
//     */
//    private Map<String, String> envVariables;

}
