package org.entando.kubernetes.service.assembler;

import org.entando.kubernetes.controller.EntandoComponentBundleController;
import org.entando.kubernetes.controller.ObservedNamespaceController;
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class EntandoComponentBundleResourceAssembler implements
        RepresentationModelAssembler<EntandoComponentBundle, EntityModel<EntandoComponentBundle>> {

    @Override
    public EntityModel<EntandoComponentBundle> toModel(EntandoComponentBundle bundle) {
        EntityModel<EntandoComponentBundle> response = new EntityModel<>(bundle);
        response.add(getLinks(bundle));
        return response;
    }

    private Links getLinks(EntandoComponentBundle bundle) {
        String bundleName = bundle.getMetadata().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        return Links.of(
            linkTo(methodOn(EntandoComponentBundleController.class).get(bundleName)).withSelfRel(),
            linkTo(methodOn(EntandoComponentBundleController.class).list()).withRel("bundles"),
            linkTo(methodOn(EntandoComponentBundleController.class).listInNamespace(bundleNamespace)).withRel("bundles-in-namespace"),
            linkTo(methodOn(ObservedNamespaceController.class).getByName(bundleNamespace)).withRel("namespace")
        );
    }
}
