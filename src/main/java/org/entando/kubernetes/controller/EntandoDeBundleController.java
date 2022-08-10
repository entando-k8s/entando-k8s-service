package org.entando.kubernetes.controller;

import static java.util.Optional.ofNullable;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.entando.kubernetes.service.assembler.EntandoDeBundleResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bundles")
public class EntandoDeBundleController {

    public static final String BUNDLE_TYPE_ANNOTATION = "bundle.entando.org/type";
    public static final String BUNDLE_TYPE_REQUEST_PARAM = "type";
    private final EntandoDeBundleResourceAssembler resourceAssembler;
    private final EntandoDeBundleService bundleService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> list(
            @RequestParam(value = "namespace", required = false) String namespace,
            @RequestParam(value = BUNDLE_TYPE_REQUEST_PARAM, required = false) String type) {

        log.info("Listing available entando-de-bundles in {} namespace", StringUtils.isEmpty(namespace) ? "all" :
                namespace);

        List<EntandoDeBundle> deBundlesToFilter = ofNullable(namespace)
                .map(bundleService::getAllInNamespace)
                .orElseGet(bundleService::getAll);

        List<EntandoDeBundle> deBundles = ofNullable(type)
                .map(t -> filterByAnnotationBundleType(t, deBundlesToFilter))
                .orElse(deBundlesToFilter);

        return ResponseEntity.ok(getCollectionWithLinks(deBundles));
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> get(@PathVariable String name,
            @RequestParam(value = "namespace", required = false) String namespace) {

        if (StringUtils.isEmpty(namespace)) {
            log.info("Getting entando-de-bundle with name {} in observed namespaces", name);
        } else {
            log.info("Getting entando-de-bundle with name {} in namespace {}", name, namespace);
        }

        EntandoDeBundle bundle = ofNullable(namespace)
                .flatMap(ns -> bundleService.findByNameAndNamespace(name, ns))
                .or(() -> bundleService.findByName(name))
                .orElseThrow(() -> NotFoundExceptionFactory.entandoDeBundle(name));
        return ResponseEntity.ok(resourceAssembler.toModel(bundle));
    }

    @PostMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> create(@RequestBody EntandoDeBundle entandoDeBundle) {
        return ResponseEntity.ok(resourceAssembler.toModel(bundleService.createBundle(entandoDeBundle)));
    }

    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable String name) {

        log.info("Deleting {} EntandoDeBundle", name);

        bundleService.deleteBundle(name);
        return ResponseEntity.noContent().build();
    }

    private CollectionModel<EntityModel<EntandoDeBundle>> getCollectionWithLinks(List<EntandoDeBundle> deBundles) {
        return CollectionModel.of(
                deBundles.stream()
                        .map(resourceAssembler::toModel)
                        .collect(Collectors.toList()), getCollectionLinks());
    }

    private Links getCollectionLinks() {
        return Links.of(
                linkTo(methodOn(EntandoDeBundleController.class).get(null, null)).withRel("bundle"),
                linkTo(methodOn(EntandoDeBundleController.class).list(null, null)).withRel("bundles-list")
        );
    }

    private List<EntandoDeBundle> filterByAnnotationBundleType(String type, List<EntandoDeBundle> bundles) {
        return bundles.stream()
                .filter(b -> b.getMetadata().getAnnotations() != null && StringUtils.equals(type,
                        b.getMetadata().getAnnotations().get(BUNDLE_TYPE_ANNOTATION)))
                .collect(Collectors.toList());
    }
}
