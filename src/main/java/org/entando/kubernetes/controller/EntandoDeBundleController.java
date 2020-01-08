package org.entando.kubernetes.controller;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleResourceAssembler;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Resources<Resource<EntandoDeBundle>>> list(
            @RequestParam(value = "namespace", required = false, defaultValue = "") String namespace,
            @RequestParam(value = "name", required = false, defaultValue = "") String name) {
        log.info("Listing available digital-exchange bundles in " + (namespace.isEmpty() ? "any" : namespace)  + " namespace");
        List<EntandoDeBundle> deBundles;
        if (isNotBlank(name) && isNotBlank(namespace)) {
            deBundles = entandoBundleService.findBundlesByNameAndNamespace(name, namespace);
        } else if (isNotBlank(name)) {
            deBundles = entandoBundleService.findBundlesByName(name);
        } else if (isNotBlank(namespace)) {
            deBundles = entandoBundleService.getAllBundlesInNamespace(namespace);
        } else {
            deBundles = entandoBundleService.getAllBundles();
        }
        return ResponseEntity
                .ok(new Resources<>(deBundles.stream().map(resourceAssembler::toResource).collect(Collectors.toList())));
    }



}
