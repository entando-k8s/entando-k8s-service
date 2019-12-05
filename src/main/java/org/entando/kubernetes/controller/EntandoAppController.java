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
import org.entando.kubernetes.service.EntandoAppPluginLinkResourceAssembler;
import org.entando.kubernetes.service.EntandoAppResourceAssembler;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/apps")
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class EntandoAppController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final EntandoAppService entandoAppService;
    private final EntandoLinkService entandoLinkService;
    private final EntandoPluginService entandoPluginService;
    private final EntandoAppResourceAssembler appResourceAssembler;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;

    @GetMapping(path = "", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoApp>>> list() {
        log.info("Listing all deployed plugins in any namespace");
        List<EntandoApp> entandoApps = entandoAppService.listEntandoApps();
        return ResponseEntity.ok(new Resources<>(
                entandoApps.stream().map(appResourceAssembler::toResource).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{namespace}", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoApp>>> listInNamespace(@PathVariable final String namespace) {
        log.debug("Listing deployed apps in namespace {}", namespace);
        List<EntandoApp> entandoApps = entandoAppService.listEntandoAppsInNamespace(namespace);
        return ResponseEntity.ok(new Resources<>(
                entandoApps.stream().map(appResourceAssembler::toResource).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{namespace}/{name}", produces = JSON)
    public ResponseEntity<Resource<EntandoApp>> get(@PathVariable String namespace, @PathVariable String name) {
        log.debug("Requesting app with name {} in namespace {}", name, namespace);
        Optional<EntandoApp> entandoApp = entandoAppService.findAppByNameAndNamespace(name, namespace);
        return ResponseEntity
                .ok(appResourceAssembler.toResource(entandoApp.orElseThrow(NotFoundExceptionFactory::entandoApp)));
    }

    @GetMapping(path = "/{namespace}/{name}/links", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoAppPluginLink>>> listLinks(
            @PathVariable("namespace") String namespace, @PathVariable("name") String name) {
        EntandoApp entandoApp = entandoAppService
                .findAppByNameAndNamespace(name, namespace)
                .orElseThrow(NotFoundExceptionFactory::entandoApp);
        List<EntandoAppPluginLink> appLinks = entandoLinkService.listAppLinks(entandoApp);
        List<Resource<EntandoAppPluginLink>> linkResources = appLinks.stream()
                .map(linkResourceAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Resources<>(linkResources));
    }

    @PostMapping(path = "/{namespace}/{name}/links", consumes = JSON, produces = JSON)
    public ResponseEntity<Resource<EntandoAppPluginLink>> linkToPlugin(
            @PathVariable("namespace") String namespace, @PathVariable("name") String name,
            @RequestBody EntandoPlugin entandoPlugin) {
        EntandoPlugin plugin = deployPluginIfNotAvailableOnCluster(entandoPlugin, namespace);
        EntandoApp entandoApp = entandoAppService.findAppByNameAndNamespace(name, namespace)
                .orElseThrow(NotFoundExceptionFactory::entandoApp);
        EntandoAppPluginLink newLink = entandoLinkService.generateForAppAndPlugin(entandoApp, plugin);
        EntandoAppPluginLink deployedLink = entandoLinkService.deploy(newLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkResourceAssembler.toResource(deployedLink));
    }

    @DeleteMapping(path = "/{namespace}/{name}/links/{pluginId}", produces = JSON)
    public ResponseEntity delete(@PathVariable("namespace") String namespace, @PathVariable("name") String name,
            @PathVariable("pluginId") String pluginId) {
        List<EntandoAppPluginLink> appLinks = entandoLinkService.listEntandoAppLinks(namespace, name);
        Optional<EntandoAppPluginLink> linkToRemove = appLinks.stream()
                .filter(el -> el.getSpec().getEntandoPluginName().equals(pluginId)).findFirst();
        linkToRemove.ifPresent(entandoLinkService::delete);
        return ResponseEntity.accepted().build();
    }

    private EntandoPlugin deployPluginIfNotAvailableOnCluster(EntandoPlugin plugin, String fallbackNamespace) {
        String pluginName = plugin.getMetadata().getName();
        Optional<EntandoPlugin> optionalPlugin = entandoPluginService.findPluginById(pluginName);
        return optionalPlugin.orElseGet(() -> {
            String pluginNamespace = Optional.ofNullable(plugin.getMetadata().getNamespace())
                    .filter(ns -> !ns.isEmpty())
                    .orElse(fallbackNamespace);
            plugin.getMetadata().setNamespace(pluginNamespace);
            return entandoPluginService.deploy(plugin);
        });

    }

}
