package org.entando.kubernetes.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppPluginLinkResourceAssembler implements
        ResourceAssembler<EntandoAppPluginLink, Resource<EntandoAppPluginLink>> {


    @Override
    public Resource<EntandoAppPluginLink> toResource(EntandoAppPluginLink entity) {
        Resource<EntandoAppPluginLink> response = new Resource<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(Resource<EntandoAppPluginLink> response) {
        EntandoAppPluginLink link = response.getContent();
        response.add(linkTo(methodOn(EntandoAppController.class)
                .get(link.getSpec().getEntandoAppNamespace(), link.getSpec().getEntandoAppName())).withRel("app"));
        response.add(linkTo(methodOn(EntandoPluginController.class)
                .get(response.getContent().getSpec().getEntandoPluginName())).withRel("plugin"));
    }
}
