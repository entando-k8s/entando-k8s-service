package org.entando.kubernetes.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.assembler.EntandoPluginResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
public class EntandoPluginController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final EntandoPluginService entandoPluginService;
    private final EntandoPluginResourceAssembler resourceAssembler;

    public EntandoPluginController(EntandoPluginService entandoPluginService,
            EntandoPluginResourceAssembler resourceAssembler) {
        this.entandoPluginService = entandoPluginService;
        this.resourceAssembler = resourceAssembler;
    }

    @GetMapping(path = "", produces = JSON)
    public ResponseEntity<CollectionModel<EntityModel<EntandoPlugin>>> list(
            @RequestParam(value = "namespace", required = false, defaultValue = "") String namespace) {
        log.info("Listing all deployed plugins in any namespace");
        List<EntandoPlugin> plugins;
        if (Strings.isEmpty(namespace)) {
            plugins = entandoPluginService.getAllPlugins();
        } else {
            plugins = entandoPluginService.getAllPluginsInNamespace(namespace);
        }
        return ResponseEntity
                .ok(new CollectionModel<>(plugins.stream().map(resourceAssembler::toModel).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity<EntityModel<EntandoPlugin>> get(@PathVariable final String pluginId) {
        log.info("Requesting plugins with identifier {} in any namespace", pluginId);
        Optional<EntandoPlugin> plugin = entandoPluginService.findPluginById(pluginId);
        return ResponseEntity
                .ok(resourceAssembler.toModel(plugin.orElseThrow(NotFoundExceptionFactory::entandoPlugin)));
    }

    @DeleteMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity delete(@PathVariable String pluginId) {
        log.info("Deleting plugin with identifier {}", pluginId);
        entandoPluginService.deletePlugin(pluginId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = JSON, produces = JSON)
    public ResponseEntity<EntityModel<EntandoPlugin>> create(@RequestBody EntandoPlugin entandoPlugin) {
        throwExceptionIfAlreadyDeployed(entandoPlugin);
        EntandoPlugin deployedPlugin = entandoPluginService.deploy(entandoPlugin);
        URI resourceLink = linkTo(methodOn(getClass()).get(deployedPlugin.getMetadata().getName())).toUri();
        return ResponseEntity.created(resourceLink).body(resourceAssembler.toModel(deployedPlugin));
    }

    private void throwExceptionIfAlreadyDeployed(EntandoPlugin entandoPlugin) {
        Optional<EntandoPlugin> alreadyDeployedPlugin = entandoPluginService.findPluginByIdAndNamespace(
                entandoPlugin.getMetadata().getName(), entandoPlugin.getMetadata().getNamespace());
        if (alreadyDeployedPlugin.isPresent()) {
            throw BadRequestExceptionFactory.pluginAlreadyDeployed(alreadyDeployedPlugin.get());
        }
    }

}
