package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
import org.springframework.hateoas.Links;
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

    private final EntandoDeBundleResourceAssembler resourceAssembler;
    private final EntandoDeBundleService bundleService;

    @GetMapping(path = "", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> list() {
        log.info("Listing available digital-exchange bundles");
        List<EntandoDeBundle> deBundles = bundleService.getAll();
        return ResponseEntity
                .ok(getCollectionWithLinks(deBundles));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = {
            "namespace"})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> listInNamespace(
            @RequestParam("namespace") String namespace) {
        log.info("Listing available entando-de-bundles in namespace {}", namespace);
        List<EntandoDeBundle> deBundles = bundleService.getAllInNamespace(namespace);
        CollectionModel<EntityModel<EntandoDeBundle>> collection = getCollectionWithLinks(deBundles);
        return ResponseEntity.ok(collection);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> get(@PathVariable String name) {
        log.info("Getting entando-de-bundle with name {} in observed namespaces", name);
        EntandoDeBundle bundle = getBundleOrFail(name);
        return ResponseEntity.ok(resourceAssembler.toModel(bundle));
    }

    private CollectionModel<EntityModel<EntandoDeBundle>> getCollectionWithLinks(List<EntandoDeBundle> deBundles) {
        CollectionModel<EntityModel<EntandoDeBundle>> c =new CollectionModel<>(
                deBundles.stream()
                        .map(resourceAssembler::toModel)
                        .collect(Collectors.toList()));
        c.add(getCollectionLinks());
        return c;
    }

    private Links getCollectionLinks() {
        return Links.of(
                linkTo(methodOn(EntandoDeBundleController.class).get(null)).withRel("bundle"),
                linkTo(methodOn(EntandoDeBundleController.class).listInNamespace(null)).withRel("bundles-in-namespace")
        );
    }


    private EntandoDeBundle getBundleOrFail(String name) {
        Optional<EntandoDeBundle> ob = bundleService.findByName(name);
        return ob.orElseThrow(() -> NotFoundExceptionFactory.entandoDeBundle(name));
    }


}
