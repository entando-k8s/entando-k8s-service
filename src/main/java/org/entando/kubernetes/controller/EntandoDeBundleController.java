package org.entando.kubernetes.controller;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleService;
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

    private final EntandoDeBundleService bundleService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoDeBundle>> list(
            @RequestParam(value = "namespace", required = false) String namespace,
            @RequestParam(value = "repoUrl", required = false) String repoUrl) {

        log.info("Listing available entando-de-bundles in {} namespace with repoUrl:'{}'",
                StringUtils.isEmpty(namespace) ? "all" :
                        namespace, repoUrl);

        List<EntandoDeBundle> deBundles = ofNullable(namespace)
                .map(bundleService::getAllInNamespace)
                .orElseGet(bundleService::getAll);

        return ResponseEntity.ok(
                ofNullable(repoUrl).map(r ->
                                deBundles.stream().filter(b -> bundleContainsRepoUrl(b, r)).collect(Collectors.toList()))
                        .orElse(deBundles));
    }

    private boolean bundleContainsRepoUrl(EntandoDeBundle bundle, String repoUrl) {
        return bundle.getSpec().getTags().stream().anyMatch(t -> StringUtils.equals(repoUrl, t.getTarball()));
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoDeBundle> get(@PathVariable String name,
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
        return ResponseEntity.ok(bundle);
    }

    @PostMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoDeBundle> create(@RequestBody EntandoDeBundle entandoDeBundle) {
        return ResponseEntity.ok(bundleService.createBundle(entandoDeBundle));
    }

    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable String name) {

        log.info("Deleting {} EntandoDeBundle", name);

        bundleService.deleteBundle(name);
        return ResponseEntity.noContent().build();
    }


}
