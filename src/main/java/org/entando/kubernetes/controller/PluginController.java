package org.entando.kubernetes.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.entando.kubernetes.exception.PluginNotFoundException;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoPluginResourceAssembler;
import org.entando.kubernetes.service.KubernetesService;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@Slf4j
@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class PluginController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    private final @NonNull KubernetesService kubernetesService;
    private final @NonNull EntandoPluginResourceAssembler resourceAssembler;

    @GetMapping(path = "", produces = JSON)
    public ResponseEntity<Resources<Resource<EntandoPlugin>>> list(@RequestParam(value = "namespace", required = false, defaultValue = "") String namespace )  {
        log.info("Listing all deployed plugins in any namespace");
        List<EntandoPlugin> plugins;
        if (Strings.isEmpty(namespace)) {
            plugins = kubernetesService.getAllPlugins();
        } else {
            plugins = kubernetesService.getAllPluginsInNamespace(namespace);
        }
        return ResponseEntity.ok(new Resources<>(plugins.stream().map(resourceAssembler::toResource).collect(Collectors.toList())));
    }

    @GetMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity<Resource<EntandoPlugin>> get(@PathVariable final String pluginId)  {
        log.info("Requesting plugins with identifier {} in any namespace", pluginId);
        Optional<EntandoPlugin> plugin = kubernetesService.findPluginById(pluginId);
        return ResponseEntity.ok(resourceAssembler.toResource(plugin.orElseThrow(PluginNotFoundException::new)));
    }

    @DeleteMapping(path = "/{pluginId}", produces = JSON)
    public ResponseEntity delete(@PathVariable String pluginId) {
        log.info("Deleting plugin with identifier {}", pluginId);
        kubernetesService.deletePlugin(pluginId);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = JSON, produces = JSON)
    public ResponseEntity<Resource<EntandoPlugin>> create(@RequestBody EntandoPlugin entandoPlugin) {
        EntandoPlugin deployedPlugin = kubernetesService.deploy(entandoPlugin);
        URI resourceLink = linkTo(methodOn(getClass()).get(deployedPlugin.getMetadata().getName())).toUri();
        return ResponseEntity.created(resourceLink).body(resourceAssembler.toResource(deployedPlugin));
    }


}
