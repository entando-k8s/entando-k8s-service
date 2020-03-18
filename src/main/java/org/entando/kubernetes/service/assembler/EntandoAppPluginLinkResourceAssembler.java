package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.controller.KubernetesNamespaceController;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppPluginLinkResourceAssembler implements
        RepresentationModelAssembler<EntandoAppPluginLink, EntityModel<EntandoAppPluginLink>> {


    @Override
    public EntityModel<EntandoAppPluginLink> toModel(EntandoAppPluginLink entity) {
        EntityModel<EntandoAppPluginLink> response = new EntityModel<>(entity);

        response.add(getLinks(entity));

        return response;

    }

    private Links getLinks(EntandoAppPluginLink link) {
        String pluginName = link.getSpec().getEntandoPluginName();
        String appName = link.getSpec().getEntandoAppName();
        return Links.of(
            linkTo(methodOn(EntandoAppController.class).get(appName)).withRel("app"),
            linkTo(methodOn(EntandoPluginController.class).get(pluginName)).withRel("plugin"),
            linkTo(methodOn(EntandoAppController.class).unlink(appName, pluginName)).withRel("unlink"),
            linkTo(methodOn(KubernetesNamespaceController.class).getByName(link.getMetadata().getNamespace())).withRel("namespace")
        );
    }
}
