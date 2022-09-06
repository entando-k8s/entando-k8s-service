package org.entando.kubernetes.service.assembler;

import static org.entando.kubernetes.controller.EntandoDeBundleController.ECR_INSTALL_CAUSE_ANNOTATION;
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
        String ecrInstallCauseAnnotation =
                bundle.getMetadata().getAnnotations() != null ? bundle.getMetadata().getAnnotations()
                        .get(ECR_INSTALL_CAUSE_ANNOTATION) : null;

        return Links.of(
                linkTo(methodOn(EntandoDeBundleController.class).get(bundleName, bundleNamespace)).withSelfRel(),
                linkTo(methodOn(EntandoDeBundleController.class).list(bundleNamespace,
                        ecrInstallCauseAnnotation)).withRel(
                        "bundles"),
                linkTo(methodOn(ObservedNamespaceController.class).getByName(bundleNamespace)).withRel("namespace")
        );
    }
}
