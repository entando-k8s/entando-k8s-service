package org.entando.kubernetes.service;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.model.app.EntandoApp;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Service;

@Service
public class EntandoAppResourceAssembler implements ResourceAssembler<EntandoApp, Resource<EntandoApp>> {


    @Override
    public Resource<EntandoApp> toResource(EntandoApp entity) {
        Resource<EntandoApp> response = new Resource<>(entity);

        applyRel(response);

        return response;

    }

    private void applyRel(Resource<EntandoApp> response) {
        String appName = response.getContent().getMetadata().getName();
        String appNamespace = response.getContent().getMetadata().getNamespace();
        response.add(linkTo(methodOn(EntandoAppController.class).get(appNamespace, appName))
                .withSelfRel());
    }
}
