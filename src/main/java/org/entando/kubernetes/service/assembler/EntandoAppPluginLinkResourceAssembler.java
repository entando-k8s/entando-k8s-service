package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.app.EntandoAppController;
import org.entando.kubernetes.controller.plugin.EntandoPluginController;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppPluginLinkResourceAssembler implements
        RepresentationModelAssembler<EntandoAppPluginLink, EntityModel<EntandoAppPluginLink>> {


    @Override
    public EntityModel<EntandoAppPluginLink> toModel(EntandoAppPluginLink entity) {
        EntityModel<EntandoAppPluginLink> response = new EntityModel<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(EntityModel<EntandoAppPluginLink> response) {
        EntandoAppPluginLink link = response.getContent();
        assert link != null;
        String pluginName = link.getSpec().getEntandoPluginName();
        String pluginNamespace = link.getSpec().getEntandoPluginNamespace();
        String appName = link.getSpec().getEntandoAppName();
        String appNamespace = link.getSpec().getEntandoAppNamespace();

        response.add(linkTo(methodOn(EntandoAppController.class)
                .get(appName)).withRel("app"));
        response.add(linkTo(methodOn(EntandoPluginController.class)
                .get(pluginName)).withRel("plugin"));
    }
}
