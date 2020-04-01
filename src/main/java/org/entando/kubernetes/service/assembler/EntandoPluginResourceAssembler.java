package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.controller.ObservedNamespaceController;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoPluginResourceAssembler implements
        RepresentationModelAssembler<EntandoPlugin, EntityModel<EntandoPlugin>> {


    @Override
    public EntityModel<EntandoPlugin> toModel(EntandoPlugin entity) {
        EntityModel<EntandoPlugin> response = new EntityModel<>(entity);
        response.add(getLinks(entity));
        return response;

    }

    private Links getLinks(EntandoPlugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        String pluginNamespace = plugin.getMetadata().getNamespace();
        return Links.of(
            linkTo(methodOn(EntandoPluginController.class).get(pluginName)).withSelfRel(),
            linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"),
            linkTo(methodOn(EntandoPluginController.class).listInNamespace(pluginNamespace)).withRel("plugins-in-namespace"),
            linkTo(methodOn(EntandoPluginController.class).getPluginIngress(pluginName)).withRel("plugin-ingress"),
            linkTo(methodOn(ObservedNamespaceController.class).getByName(pluginNamespace)).withRel("namespace")
        );
    }
}
