package org.entando.kubernetes.controller;

import io.fabric8.kubernetes.api.model.Namespace;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.service.NamespaceService;
import org.entando.kubernetes.service.assembler.NamespaceResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/namespaces")
public class NamespaceController {

    private final NamespaceResourceAssembler nsResourceAssembler;
    private final NamespaceService nsService;

    public NamespaceController(NamespaceResourceAssembler nrs, NamespaceService nsService) {
       this.nsResourceAssembler = nrs;
       this.nsService = nsService;
    }

    public ResponseEntity<CollectionModel<EntityModel<Namespace>>> list() {
        List<Namespace> observedNamespaces = nsService.getObservedNamespaceList();
        CollectionModel<EntityModel<Namespace>> nsCollection = new CollectionModel<>(
                observedNamespaces.stream()
                        .map(nsResourceAssembler::toModel)
                        .collect(Collectors.toList()));
        return ResponseEntity.ok(nsCollection);
    }

    @GetMapping("/{name}")
    public ResponseEntity<EntityModel<Namespace>> getByName(@PathVariable("name") String name) {
        Optional<Namespace> observedNs = nsService.getObservedNamespace(name);
        if (!observedNs.isPresent()) {
            throw NotFoundExceptionFactory.observedNamespace(name);
        }
        EntityModel<Namespace> ns = nsResourceAssembler.toModel(observedNs.get());
        return ResponseEntity.ok(ns);
    }
}
