package org.entando.kubernetes.service.assembler;

import io.fabric8.kubernetes.api.model.Namespace;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class NamespaceResourceAssembler implements
        RepresentationModelAssembler<Namespace, EntityModel<Namespace>> {

    @Override
    public EntityModel<Namespace> toModel(Namespace entity) {
        return new EntityModel<>(entity);
    }
}
