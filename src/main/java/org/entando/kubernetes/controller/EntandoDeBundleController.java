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
import org.entando.kubernetes.model.common.EntandoMultiTenancy;
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

    private final EntandoDeBundleResourceAssembler resourceAssembler;
    private final EntandoDeBundleService bundleService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> list(
            @RequestParam(value = "namespace", required = false) String namespace,
            @RequestParam(value = "repoUrl", required = false) String repoUrl,
            @RequestParam(value = "tenantCode", required = false) String tenantCode) {

        final String tenantCodeOrDefault = getTenantOrDefault(tenantCode);
        log.info("Listing available entando-de-bundles for tenantCode:'{}' in {} namespace with repoUrl:'{}'",
                tenantCode,
                StringUtils.isEmpty(namespace) ? "all" :
                        namespace, repoUrl);

        List<EntandoDeBundle> deBundles = ofNullable(namespace)
                .map(n -> bundleService.getAllInNamespace(n, tenantCodeOrDefault))
                .orElseGet(() -> bundleService.getAll(tenantCodeOrDefault));

        return ResponseEntity.ok(getCollectionWithLinks(
                ofNullable(repoUrl).map(r ->
                                deBundles.stream().filter(b -> bundleContainsRepoUrl(b, r)).collect(Collectors.toList()))
                        .orElse(deBundles)));
    }

    private boolean bundleContainsRepoUrl(EntandoDeBundle bundle, String repoUrl) {
        return bundle.getSpec().getTags().stream().anyMatch(t -> StringUtils.equals(repoUrl, t.getTarball()));
    }

    private String getTenantOrDefault(String tenantCode) {
        return ofNullable(tenantCode)
                .filter(StringUtils::isNotBlank)
                .orElse(EntandoMultiTenancy.PRIMARY_TENANT);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> get(@PathVariable String name,
                                                            @RequestParam(value = "namespace", required = false) String namespace,
                                                            @RequestParam(value = "tenantCode", required = false) String tenantCode) {

        final String tenantCodeOrDefault = getTenantOrDefault(tenantCode);
        if (StringUtils.isEmpty(namespace)) {
            log.info("Getting entando-de-bundle for tenantCode:'{}' with name {} in observed namespaces", tenantCodeOrDefault, name);
        } else {
            log.info("Getting entando-de-bundle for tenantCode:'{}' with name {} in namespace {}", tenantCodeOrDefault, name, namespace);
        }

        EntandoDeBundle bundle = ofNullable(namespace)
                .flatMap(ns -> bundleService.findByNameAndNamespace(name, ns, tenantCodeOrDefault))
                .or(() -> bundleService.findByName(name, tenantCodeOrDefault))
                .orElseThrow(() -> NotFoundExceptionFactory.entandoDeBundle(name));
        return ResponseEntity.ok(resourceAssembler.toModel(bundle));
    }

    @PostMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoDeBundle>> create(@RequestBody EntandoDeBundle entandoDeBundle,
                                                               @RequestParam(value = "tenantCode", required = false) String tenantCode) {

        final String tenantCodeOrDefault = getTenantOrDefault(tenantCode);
        log.info("Creating '{}' EntandoDeBundle for tenantCode:'{}'", entandoDeBundle.getMetadata().getName(), tenantCodeOrDefault);
        return ResponseEntity.ok(resourceAssembler.toModel(bundleService.createBundle(entandoDeBundle, tenantCodeOrDefault)));
    }

    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable String name,
                                       @RequestParam(value = "tenantCode", required = false) String tenantCode) {

        final String tenantCodeOrDefault = getTenantOrDefault(tenantCode);
        log.info("Deleting {} EntandoDeBundle for tenantCode:'{}'", name, tenantCodeOrDefault);

        bundleService.deleteBundle(name, tenantCodeOrDefault);
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
                linkTo(methodOn(EntandoDeBundleController.class).get(null, null, null)).withRel("bundle"),
                linkTo(methodOn(EntandoDeBundleController.class).list(null, null, null)).withRel("bundles-list")
        );
    }

}
