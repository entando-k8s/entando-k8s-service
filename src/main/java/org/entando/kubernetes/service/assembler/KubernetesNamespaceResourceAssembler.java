package org.entando.kubernetes.service.assembler;

import static org.entando.kubernetes.controller.EntandoDeBundleController.BUNDLE_TYPE_REQUEST_PARAM;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.Map;
import org.entando.kubernetes.controller.EntandoAppController;
import org.entando.kubernetes.controller.EntandoDeBundleController;
import org.entando.kubernetes.controller.EntandoLinksController;
import org.entando.kubernetes.controller.EntandoPluginController;
import org.entando.kubernetes.controller.ObservedNamespaceController;
import org.entando.kubernetes.model.namespace.ObservedNamespace;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class KubernetesNamespaceResourceAssembler implements
        RepresentationModelAssembler<ObservedNamespace, EntityModel<ObservedNamespace>> {

    @Override
    public EntityModel<ObservedNamespace> toModel(ObservedNamespace ons) {

        EntityModel<ObservedNamespace> em = new EntityModel<>(ons);

        String ns = ons.getName();
        em.add(linkTo(methodOn(ObservedNamespaceController.class).getByName(ns)).withSelfRel());
        em.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(ns)).withRel("plugins-in-namespace"));
        em.add(linkTo(methodOn(EntandoAppController.class).listInNamespace(ns)).withRel("apps-in-namespace"));

        Link link = linkTo(methodOn(EntandoDeBundleController.class).list(ns, null)).withRel("bundles-in-namespace");
        // trick to remove param null to grant backward compatibility
        Map<String, String> parameters = new HashMap<>();
        parameters.put(BUNDLE_TYPE_REQUEST_PARAM, null);
        em.add(link.expand(parameters));

        em.add(linkTo(methodOn(EntandoLinksController.class).listInNamespace(ns)).withRel(
                "app-plugin-links-in-namespace"));
        return em;
    }

}
