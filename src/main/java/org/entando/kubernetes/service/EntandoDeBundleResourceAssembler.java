package org.entando.kubernetes.service;

import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class EntandoDeBundleResourceAssembler implements ResourceAssembler<EntandoDeBundle, Resource<EntandoDeBundle>> {

    @Override
    public Resource<EntandoDeBundle> toResource(EntandoDeBundle entandoDebundle) {
        return new Resource<>(entandoDebundle);
    }
}
