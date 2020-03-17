package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.entando.kubernetes.service.assembler.EntandoDeBundleResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bundles")
public class EntandoDeBundleController {

    private final EntandoDeBundleService entandoBundleService;
    private final EntandoDeBundleResourceAssembler resourceAssembler;

    @GetMapping(path = "", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> list() {
        log.info("Listing available digital-exchange bundles");
        List<EntandoDeBundle> deBundles = entandoBundleService.getAll();
        return ResponseEntity
                .ok(new CollectionModel<>(deBundles.stream().map(resourceAssembler::toModel).collect(Collectors.toList())));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = {
            "namespace"})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> listInNamespace(
            @RequestParam("namespace") String namespace) {
        log.info("Listing available entando-de-bundles in namespace {}", namespace);
        List<EntandoDeBundle> deBundles = entandoBundleService.getAllInNamespace(namespace);
        return ResponseEntity.ok(new CollectionModel<>(
                deBundles.stream()
                        .map(resourceAssembler::toModel)
                        .collect(Collectors.toList())));
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> get(@PathVariable String name) {
        log.info("Getting entando-de-bundle with name {} in observed namespaces", name);
        EntandoDeBundle bundle = getBundleOrFail(name);
        return ResponseEntity.ok(resourceAssembler.toModel(bundle));
    }

    private EntandoDeBundle getBundleOrFail(String name) {
        Optional<EntandoDeBundle> ob = entandoBundleService.findByName(name);
        return ob.orElseThrow(() -> NotFoundExceptionFactory.entandoDeBundle(name));
    }


}
