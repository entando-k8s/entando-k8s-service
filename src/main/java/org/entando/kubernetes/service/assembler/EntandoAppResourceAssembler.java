package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.model.app.EntandoApp;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppResourceAssembler implements RepresentationModelAssembler<EntandoApp, EntityModel<EntandoApp>> {


    @Override
    public EntityModel<EntandoApp> toModel(EntandoApp entity) {
        EntityModel<EntandoApp> response = new EntityModel<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(EntityModel<EntandoApp> response) {
        String appName = response.getContent().getMetadata().getName();
        String appNamespace = response.getContent().getMetadata().getNamespace();
        response.add(linkTo(methodOn(EntandoAppController.class).get(appNamespace, appName))
                .withSelfRel());
    }
}
