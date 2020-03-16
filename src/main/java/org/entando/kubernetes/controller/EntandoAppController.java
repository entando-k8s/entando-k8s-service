package org.entando.kubernetes.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.entando.kubernetes.service.assembler.EntandoAppResourceAssembler;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/apps")
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class EntandoAppController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String HAL_JSON = MediaTypes.HAL_JSON_VALUE;

    private final EntandoAppService entandoAppService;
    private final EntandoLinkService entandoLinkService;
    private final EntandoPluginService entandoPluginService;
    private final EntandoAppResourceAssembler appResourceAssembler;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;
    private final KubernetesUtils k8sUtils;

    @GetMapping(produces = {JSON,HAL_JSON})
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> list() {
        log.info("Listing apps from all observed namespaces");
        List<EntandoApp> entandoApps = entandoAppService.getApps();
        return ResponseEntity.ok(new CollectionModel<>(
                entandoApps.stream().map(appResourceAssembler::toModel).collect(Collectors.toList())));
    }


    @GetMapping(produces = {JSON,HAL_JSON}, params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing apps");
        List<EntandoApp> entandoApps = entandoAppService.getAppsInNamespace(namespace);
        return ResponseEntity.ok(new CollectionModel<>(
                entandoApps.stream().map(appResourceAssembler::toModel).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{name}", produces = {JSON,HAL_JSON})
    public ResponseEntity<EntityModel<EntandoApp>> get(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        return ResponseEntity.ok(appResourceAssembler.toModel(entandoApp));
    }

    @GetMapping(path = "/{name}/links", produces = {JSON,HAL_JSON})
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listLinks(
            @PathVariable("name") String appName) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        List<EntandoAppPluginLink> appLinks = entandoLinkService.listAppLinks(entandoApp);
        List<EntityModel<EntandoAppPluginLink>> linkResources = appLinks.stream()
                .map(linkResourceAssembler::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new CollectionModel<>(linkResources));
    }

    @PostMapping(path = "/{name}/links", consumes = JSON, produces = {JSON,HAL_JSON})
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> linkToPlugin(
            @PathVariable("name") String appName,
            @RequestBody EntandoPlugin entandoPlugin) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        EntandoPlugin plugin = getOrCreatePlugin(entandoPlugin);
        EntandoAppPluginLink newLink = entandoLinkService.generateForAppAndPlugin(entandoApp, plugin);
        EntandoAppPluginLink deployedLink = entandoLinkService.deploy(newLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkResourceAssembler.toModel(deployedLink));
    }

    @DeleteMapping(path = "/{name}/links/{pluginName}", produces = {JSON,HAL_JSON})
    public ResponseEntity<Void> delete(
            @PathVariable("name") String appName,
            @PathVariable("pluginName") String pluginName) {
        EntandoApp app = getEntandoAppOrFail(appName);
        EntandoAppPluginLink linkToRemove = getLinkOrFail(app, pluginName);
        entandoLinkService.delete(linkToRemove);
        return ResponseEntity.accepted().build();
    }

    private EntandoApp getEntandoAppOrFail(String appName) {
        return entandoAppService
                .findAppByName(appName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoApp(appName);
                });
    }

    private EntandoAppPluginLink getLinkOrFail(EntandoApp app, String pluginName) {
        return entandoLinkService.getLink(app, pluginName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoLink(app.getMetadata().getName(), pluginName);
                });
    }

    private EntandoPlugin getOrCreatePlugin(EntandoPlugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        Optional<EntandoPlugin> optionalPlugin = entandoPluginService.findPluginByName(pluginName);
        return optionalPlugin.orElseGet(() -> {
            String pluginNamespace = Optional.ofNullable(plugin.getMetadata().getNamespace())
                    .filter(ns -> !ns.isEmpty())
                    .orElse(k8sUtils.getCurrentNamespace());
            plugin.getMetadata().setNamespace(pluginNamespace);
            return entandoPluginService.deploy(plugin);
        });

    }

}
