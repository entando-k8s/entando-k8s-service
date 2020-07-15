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
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.entando.kubernetes.service.EntandoComponentBundleService;
import org.entando.kubernetes.service.assembler.EntandoComponentBundleResourceAssembler;
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
public class EntandoComponentBundleController {

    private final EntandoComponentBundleResourceAssembler resourceAssembler;
    private final EntandoComponentBundleService bundleService;

    @GetMapping(path = "", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoComponentBundle>>> list() {
        log.info("Listing available digital-exchange bundles");
        List<EntandoComponentBundle> componentBundles = bundleService.getAll();
        return ResponseEntity
                .ok(getCollectionWithLinks(componentBundles));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = {
            "namespace"})
    public ResponseEntity<CollectionModel<EntityModel<EntandoComponentBundle>>> listInNamespace(
            @RequestParam("namespace") String namespace) {
        log.info("Listing available entando-de-bundles in namespace {}", namespace);
        List<EntandoComponentBundle> componentBundles = bundleService.getAllInNamespace(namespace);
        CollectionModel<EntityModel<EntandoComponentBundle>> collection = getCollectionWithLinks(componentBundles);
        return ResponseEntity.ok(collection);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoComponentBundle>> get(@PathVariable String name) {
        log.info("Getting entando-de-bundle with name {} in observed namespaces", name);
        EntandoComponentBundle bundle = getBundleOrFail(name);
        return ResponseEntity.ok(resourceAssembler.toModel(bundle));
    }

    private CollectionModel<EntityModel<EntandoComponentBundle>> getCollectionWithLinks(List<EntandoComponentBundle> componentBundles) {
        CollectionModel<EntityModel<EntandoComponentBundle>> c =new CollectionModel<>(
                componentBundles.stream()
                        .map(resourceAssembler::toModel)
                        .collect(Collectors.toList()));
        c.add(getCollectionLinks());
        return c;
    }

    private Links getCollectionLinks() {
        return Links.of(
                linkTo(methodOn(EntandoComponentBundleController.class).get(null)).withRel("bundle"),
                linkTo(methodOn(EntandoComponentBundleController.class).listInNamespace(null)).withRel("bundles-in-namespace")
        );
    }


    private EntandoComponentBundle getBundleOrFail(String name) {
        Optional<EntandoComponentBundle> ob = bundleService.findByName(name);
        return ob.orElseThrow(() -> NotFoundExceptionFactory.entandoComponentBundle(name));
    }


}
