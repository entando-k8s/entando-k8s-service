package org.entando.kubernetes.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoPluginResourceAssembler;
import org.entando.kubernetes.service.EntandoPluginService;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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


@Slf4j
@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class EntandoPluginController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final @NonNull EntandoPluginService entandoPluginService;
    private final @NonNull EntandoPluginResourceAssembler resourceAssembler;

    @GetMapping(path = "", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoPlugin>>> list(
            @RequestParam(value = "namespace", required = false, defaultValue = "") String namespace) {
        log.info("Listing all deployed plugins in any namespace");
        List<EntandoPlugin> plugins;
        if (Strings.isEmpty(namespace)) {
            plugins = entandoPluginService.getAllPlugins();
        } else {
            plugins = entandoPluginService.getAllPluginsInNamespace(namespace);
        }
        return ResponseEntity
                .ok(new Resources<>(plugins.stream().map(resourceAssembler::toResource).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity<Resource<EntandoPlugin>> get(@PathVariable final String pluginId) {
        log.info("Requesting plugins with identifier {} in any namespace", pluginId);
        Optional<EntandoPlugin> plugin = entandoPluginService.findPluginById(pluginId);
        return ResponseEntity.ok(resourceAssembler.toResource(plugin.orElseThrow(NotFoundExceptionFactory::entandoPlugin)));
    }

    @DeleteMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity delete(@PathVariable String pluginId) {
        log.info("Deleting plugin with identifier {}", pluginId);
        entandoPluginService.deletePlugin(pluginId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = JSON, produces = JSON)
    public ResponseEntity<Resource<EntandoPlugin>> create(@RequestBody EntandoPlugin entandoPlugin) {
        throwExceptionIfAlreadyDeployed(entandoPlugin);
        EntandoPlugin deployedPlugin = entandoPluginService.deploy(entandoPlugin);
        URI resourceLink = linkTo(methodOn(getClass()).get(deployedPlugin.getMetadata().getName())).toUri();
        return ResponseEntity.created(resourceLink).body(resourceAssembler.toResource(deployedPlugin));
    }

    private void throwExceptionIfAlreadyDeployed(EntandoPlugin entandoPlugin) {
        Optional<EntandoPlugin> alreadyDeployedPlugin = entandoPluginService.findPluginByIdAndNamespace(
                entandoPlugin.getMetadata().getName(), entandoPlugin.getMetadata().getNamespace());
        if (alreadyDeployedPlugin.isPresent()) {
            throw BadRequestExceptionFactory.pluginAlreadyDeployed(alreadyDeployedPlugin.get());
        }
    }

}
