package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoPluginResourceAssembler implements
        RepresentationModelAssembler<EntandoPlugin, EntityModel<EntandoPlugin>> {


    @Override
    public EntityModel<EntandoPlugin> toModel(EntandoPlugin entity) {
        EntityModel<EntandoPlugin> response = new EntityModel<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(EntityModel<EntandoPlugin> response) {
        String pluginName = response.getContent().getMetadata().getName();
        String pluginNamespace = response.getContent().getMetadata().getNamespace();
        response.add(linkTo(methodOn(EntandoPluginController.class).get( pluginNamespace, pluginName)) .withSelfRel());
        response.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(pluginNamespace)).withRel("namespace_plugin"));
        response.add(linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"));
    }
}
