package org.entando.kubernetes.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleResourceAssembler;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/de-bundles")
public class EntandoDeBundleController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final EntandoDeBundleService entandoBundleService;
    private final EntandoDeBundleResourceAssembler resourceAssembler;

    public EntandoDeBundleController(EntandoDeBundleService entandoBundleService,
            EntandoDeBundleResourceAssembler resourceAssembler) {
        this.entandoBundleService = entandoBundleService;
        this.resourceAssembler = resourceAssembler;
    }

    @GetMapping(path = "", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoDeBundle>>> list() {
        log.info("Listing available digital-exchange bundles in any namespace");
        List<EntandoDeBundle> deBundles = entandoBundleService.getAllBundles();
        return ResponseEntity
                .ok(new Resources<>(deBundles.stream().map(resourceAssembler::toResource).collect(Collectors.toList())));
    }

    @GetMapping(path = "/namespaces/{namespace}", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoDeBundle>>> getAllBundlesInNamespace(
            @PathVariable("namespace") String namespace) {
        log.info("Listing available entando-de-bundles in namespace {}", namespace);
        List<EntandoDeBundle> deBundles = entandoBundleService.getAllBundlesInNamespace(namespace);
        return ResponseEntity.ok(new Resources<>(
                deBundles.stream()
                        .map(resourceAssembler::toResource)
                        .collect(Collectors.toList())));
    }

    @GetMapping(path = "/namespaces/{namespace}/{name}", produces = JSON)
    public ResponseEntity<Resource<EntandoDeBundle>> getBundleInNamespaceWithId(
            @PathVariable("namespace") String namespace,
            @PathVariable("name") String name) {
        log.info("Getting entando-de-bundle with name {} in namespace {}", name, namespace);
        Optional<EntandoDeBundle> ob = entandoBundleService.findBundleByNameAndNamespace(name, namespace);
        EntandoDeBundle bundle = ob.orElseThrow(() -> NotFoundExceptionFactory.entandoDeBundle(name, namespace));
        return ResponseEntity.ok(resourceAssembler.toResource(bundle));
    }


}
