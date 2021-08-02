package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespace;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.service.assembler.KubernetesNamespaceResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/namespaces")
@RequiredArgsConstructor
public class ObservedNamespaceController {

    private final KubernetesNamespaceResourceAssembler resAssembler;
    private final ObservedNamespaces observedNamespaces;


    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<ObservedNamespace>>> list() {
        CollectionModel<EntityModel<ObservedNamespace>> nsCollection = getNamespaceCollectionModel();
        addNamespaceLinks(nsCollection);
        return ResponseEntity.ok(nsCollection);
    }

    @GetMapping(value = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<ObservedNamespace>> getByName(@PathVariable String name) {
        String validNamespace = validateNamespace(name);
        return ResponseEntity.ok(resAssembler.toModel(new ObservedNamespace(validNamespace)));
    }

    public String validateNamespace(String namespace) {
        if (!namespace.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?")) {
            throw BadRequestExceptionFactory.invalidNamespace(namespace);
        }
        return namespace;
    }

    private void addNamespaceLinks(CollectionModel<EntityModel<ObservedNamespace>> nsCollection) {
        nsCollection.add(linkTo(methodOn(this.getClass()).getByName(null)).withRel("namespace"));
        nsCollection.add(linkTo(methodOn(EntandoAppController.class).listInNamespace(null)).withRel("apps-in-namespace"));
        nsCollection.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(null)).withRel("plugins-in-namespace"));
        nsCollection.add(linkTo(methodOn(EntandoDeBundleController.class).listInNamespace(null)).withRel("bundles-in-namespace"));
        nsCollection.add(linkTo(methodOn(EntandoLinksController.class).listInNamespace(null)).withRel("app-plugin-links-in-namespace"));
    }


    private CollectionModel<EntityModel<ObservedNamespace>> getNamespaceCollectionModel() {
        return new CollectionModel<>(
                observedNamespaces.getList().stream().map(resAssembler::toModel).collect(Collectors.toList())
        );
    }
}
