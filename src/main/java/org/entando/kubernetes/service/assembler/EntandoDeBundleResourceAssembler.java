package org.entando.kubernetes.service.assembler;

import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class EntandoDeBundleResourceAssembler implements
        RepresentationModelAssembler<EntandoDeBundle, EntityModel<EntandoDeBundle>> {

    @Override
    public EntityModel<EntandoDeBundle> toModel(EntandoDeBundle entandoDebundle) {
        return new EntityModel<>(entandoDebundle);
    }
}
