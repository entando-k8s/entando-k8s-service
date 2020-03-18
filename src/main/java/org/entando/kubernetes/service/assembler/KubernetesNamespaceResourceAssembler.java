package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.fabric8.kubernetes.api.model.Namespace;
import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.controller.EntandoDeBundleController;
import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.controller.KubernetesNamespaceController;
import org.entando.kubernetes.model.ObservedNamespace;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class KubernetesNamespaceResourceAssembler implements
        RepresentationModelAssembler<ObservedNamespace, EntityModel<ObservedNamespace>> {

    @Override
    public EntityModel<ObservedNamespace> toModel(ObservedNamespace ons) {

        EntityModel<ObservedNamespace> em = new EntityModel<>(ons);

        String ns = ons.getName();
        em.add(linkTo(methodOn(KubernetesNamespaceController.class).getByName(ns)).withSelfRel());
        em.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(ns)).withRel("plugins"));
        em.add(linkTo(methodOn(EntandoAppController.class).listInNamespace(ns)).withRel("apps"));
        em.add(linkTo(methodOn(EntandoDeBundleController.class).listInNamespace(ns)).withRel("bundles"));
        return em;
    }

}
