package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.entando.kubernetes.controller.EntandoDeBundleController;
import org.entando.kubernetes.controller.ObservedNamespaceController;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class EntandoDeBundleResourceAssembler implements
        RepresentationModelAssembler<EntandoDeBundle, EntityModel<EntandoDeBundle>> {

    @Override
    public EntityModel<EntandoDeBundle> toModel(EntandoDeBundle bundle) {
        EntityModel<EntandoDeBundle> response = new EntityModel<>(bundle);
        response.add(getLinks(bundle));
        return response;
    }

    private Links getLinks(EntandoDeBundle bundle) {
        String bundleName = bundle.getMetadata().getName();
        String bundleNamespace = bundle.getMetadata().getNamespace();
        return Links.of(
            linkTo(methodOn(EntandoDeBundleController.class).get(bundleName)).withSelfRel(),
            linkTo(methodOn(EntandoDeBundleController.class).list()).withRel("bundles"),
            linkTo(methodOn(EntandoDeBundleController.class).listInNamespace(bundleNamespace)).withRel("bundles-in-namespace"),
            linkTo(methodOn(ObservedNamespaceController.class).getByName(bundleNamespace)).withRel("namespace")
        );
    }
}
