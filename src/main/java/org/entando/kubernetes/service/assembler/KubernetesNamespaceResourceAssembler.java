package org.entando.kubernetes.service.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.fabric8.kubernetes.api.model.Namespace;
import org.entando.kubernetes.controller.app.EntandoAppController;
import org.entando.kubernetes.controller.bundle.EntandoDeBundleController;
import org.entando.kubernetes.controller.plugin.EntandoPluginController;
import org.entando.kubernetes.controller.namespace.KubernetesNamespaceController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class KubernetesNamespaceResourceAssembler implements
        RepresentationModelAssembler<Namespace, EntityModel<Namespace>> {

    @Override
    public EntityModel<Namespace> toModel(Namespace ns) {

        EntityModel<Namespace> em = new EntityModel<>(ns);

        String nsName = ns.getMetadata().getName();
        em.add(linkTo(methodOn(KubernetesNamespaceController.class).getByName(nsName)).withSelfRel());
        em.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(nsName)).withRel("plugins"));
        em.add(linkTo(methodOn(EntandoAppController.class).listInNamespace(nsName)).withRel("apps"));
        em.add(linkTo(methodOn(EntandoDeBundleController.class).listInNamespace(nsName)).withRel("bundles"));
        return em;
    }

}
