package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.controller.EntandoLinksController;
import org.entando.kubernetes.controller.KubernetesNamespaceController;
import org.entando.kubernetes.model.app.EntandoApp;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppResourceAssembler implements RepresentationModelAssembler<EntandoApp, EntityModel<EntandoApp>> {


    @Override
    public EntityModel<EntandoApp> toModel(EntandoApp entity) {
        EntityModel<EntandoApp> response = new EntityModel<>(entity);

        response.add(getLinks(entity));

        return response;

    }

    private Links getLinks(EntandoApp app) {
        String appName = app.getMetadata().getName();
        String appNamespace = app.getMetadata().getNamespace();
        return Links.of(
            linkTo(methodOn(EntandoAppController.class).get(appName)) .withSelfRel(),
            linkTo(methodOn(EntandoAppController.class).list()).withRel("apps"),
            linkTo(methodOn(EntandoAppController.class).listInNamespace(appNamespace)).withRel("apps-in-namespace"),
            linkTo(methodOn(EntandoLinksController.class).listByApp(appName)).withRel("app-links"),
            linkTo(methodOn(KubernetesNamespaceController.class).getByName(appNamespace)) .withRel("namespace")
        );
    }
}
