package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.IngressService;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.entando.kubernetes.service.assembler.EntandoAppResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.ThrowableProblem;


@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
@RequestMapping("/apps")
public class EntandoAppController {

    private final EntandoAppService appService;
    private final EntandoAppResourceAssembler appResourceAssembler;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;
    private final KubernetesUtils k8sUtils;
    private final EntandoLinkService linkService;
    private final IngressService ingressService;
    private final EntandoPluginService pluginService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> list() {
        log.info("Listing apps from all observed namespaces");
        List<EntandoApp> entandoApps = appService.getAll();
        CollectionModel<EntityModel<EntandoApp>> collection = getAppsCollectionModel(entandoApps);
        addAppCollectionLinks(collection);
        return ResponseEntity.ok(collection);
    }


    @GetMapping(produces = {APPLICATION_JSON_VALUE,
            HAL_JSON_VALUE}, params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing apps");
        List<EntandoApp> entandoApps = appService.getAllInNamespace(namespace);
        CollectionModel<EntityModel<EntandoApp>> collection = getAppsCollectionModel(entandoApps);
        addAppCollectionLinks(collection);
        return ResponseEntity.ok(collection);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE,
            HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoApp>> get(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        return ResponseEntity.ok(appResourceAssembler.toModel(entandoApp));
    }

    @GetMapping(path = "/{name}/ingress", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<Ingress>> getAppIngress(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        Ingress appIngress = getEntandoAppIngressOrFail(entandoApp);
        return ResponseEntity.ok(new EntityModel<>(appIngress));
    }

    @PostMapping(path = "/{name}/links", consumes = APPLICATION_JSON_VALUE, produces = {
            APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> linkToPlugin(
            @PathVariable("name") String appName,
            @RequestBody EntandoPlugin entandoPlugin) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        EntandoPlugin plugin = createPlugin(entandoPlugin);
        EntandoAppPluginLink newLink = linkService.buildBetweenAppAndPlugin(entandoApp, plugin);
        EntandoAppPluginLink deployedLink = linkService.deploy(newLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkResourceAssembler.toModel(deployedLink));
    }

    public Ingress getEntandoAppIngressOrFail(EntandoApp app) {
        return ingressService
                .findByEntandoApp(app)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.ingress(app);
                });
    }

    private EntandoApp getEntandoAppOrFail(String appName) {
        return appService
                .findByName(appName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoApp(appName);
                });
    }

    private EntandoPlugin createPlugin(EntandoPlugin newPlugin) {
        String pluginNamespace = Optional.ofNullable(newPlugin.getMetadata().getNamespace())
                .filter(ns -> !ns.isEmpty())
                .orElse(k8sUtils.getCurrentNamespace());
        newPlugin.getMetadata().setNamespace(pluginNamespace);
        return pluginService.deploy(newPlugin, EntandoPluginService.CREATE_OR_REPLACE);
    }

    private void addAppCollectionLinks(CollectionModel<EntityModel<EntandoApp>> collection) {
        collection.add(linkTo(methodOn(EntandoAppController.class).get(null)).withRel("app"));
        collection.add(linkTo(methodOn(EntandoLinksController.class).listAppLinks(null)).withRel("app-links"));
        collection.add(linkTo(methodOn(EntandoAppController.class).listInNamespace(null)).withRel("apps-in-namespace"));
    }

    private CollectionModel<EntityModel<EntandoApp>> getAppsCollectionModel(List<EntandoApp> entandoApps) {
        return new CollectionModel<>(
                entandoApps.stream().map(appResourceAssembler::toModel).collect(Collectors.toList()));
    }

}
