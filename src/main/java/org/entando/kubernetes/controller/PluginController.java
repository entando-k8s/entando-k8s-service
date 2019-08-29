package org.entando.kubernetes.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.EntandoPluginDeploymentResponse;
import org.entando.kubernetes.service.KubernetesService;
import org.entando.web.response.SimpleRestResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class PluginController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final @NonNull KubernetesService kubernetesService;

    @GetMapping(path = "", produces = JSON)
    public SimpleRestResponse<List<EntandoPluginDeploymentResponse>> list()  {
        log.info("Listing all deployed plugins");
        final List<EntandoPluginDeploymentResponse> list = kubernetesService.getDeployments();
        final SimpleRestResponse<List<EntandoPluginDeploymentResponse>> entity = new SimpleRestResponse<>();
        entity.setPayload(list);
        list.forEach(this::applyRel);
        return entity;
    }

    @GetMapping(path = "/{plugin}", produces = JSON)
    public SimpleRestResponse<EntandoPluginDeploymentResponse> get(@PathVariable final String plugin)  {
        log.info("Requesting plugin with identifier {}", plugin);
        return toResponse(kubernetesService.getDeployment(plugin));
    }

    private SimpleRestResponse<EntandoPluginDeploymentResponse> toResponse(final EntandoPluginDeploymentResponse response) {
        final SimpleRestResponse<EntandoPluginDeploymentResponse> entity = new SimpleRestResponse<>();
        entity.setPayload(response);
        entity.addMetadata("plugin", response.getPlugin());
        applyRel(response);
        return entity;
    }

    private void applyRel(final EntandoPluginDeploymentResponse response) {
        response.add(linkTo(methodOn(getClass()).get(response.getPlugin())).withSelfRel());
//        response.add(linkTo(methodOn(getClass()).get(response.getPlugin())).withRel("updateReplica"));
    }

}
