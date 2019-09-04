package org.entando.kubernetes.service;

import org.entando.kubernetes.controller.PluginController;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Service
public class EntandoPluginResourceAssembler implements ResourceAssembler<EntandoPlugin, Resource<EntandoPlugin>> {


    @Override
    public Resource<EntandoPlugin> toResource(EntandoPlugin entity) {
        Resource<EntandoPlugin> response = new Resource<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(Resource<EntandoPlugin> response) {
        response.add(linkTo(methodOn(PluginController.class).get(response.getContent().getMetadata().getName())).withSelfRel());
        response.add(linkTo(methodOn(PluginController.class).list(null)).withRel("plugins"));
    }
}
