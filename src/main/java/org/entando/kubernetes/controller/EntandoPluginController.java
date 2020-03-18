package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.entando.kubernetes.service.assembler.EntandoPluginResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class EntandoPluginController {

    private final EntandoLinkService entandoLinkService;
    private final EntandoPluginService entandoPluginService;
    private final EntandoPluginResourceAssembler resourceAssembler;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoPlugin>>> list() {
        log.info("Listing all deployed plugins in observed namespaces");
        List<EntandoPlugin> plugins = entandoPluginService.getAll();
        CollectionModel<EntityModel<EntandoPlugin>> collection = getPluginCollectionModel(plugins);
        addCollectionLinks(collection);
        return ResponseEntity.ok(collection);
    }


    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoPlugin>>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing all deployed plugins in {} observed namespace", namespace);
        //TODO: Throw an error when querying a non observed namespace
        List<EntandoPlugin> plugins = entandoPluginService.getAllInNamespace(namespace);
        CollectionModel<EntityModel<EntandoPlugin>> collection = getPluginCollectionModel(plugins);
        addCollectionLinks(collection);
        return ResponseEntity.ok(collection);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoPlugin>> get(@PathVariable("name") final String pluginName) {
        log.info("Searching plugin with name {} in observed namespaces", pluginName);
        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName);
        return ResponseEntity.ok(resourceAssembler.toModel(plugin));
    }


    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable("name") String pluginName) {
        log.info("Deleting plugin with identifier {} from observed namespaces", pluginName);
        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName);
        entandoPluginService.deletePlugin(plugin);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoPlugin>> create(
            @RequestBody EntandoPlugin entandoPlugin) {
        throwExceptionIfAlreadyDeployed(entandoPlugin);
        //TODO: throw exception if working with non observed namespace
        EntandoPlugin deployedPlugin = entandoPluginService.deploy(entandoPlugin);
        URI resourceLink = linkTo(methodOn(getClass()).get(deployedPlugin.getMetadata().getName())).toUri();
        return ResponseEntity.created(resourceLink).body(resourceAssembler.toModel(deployedPlugin));
    }

    @GetMapping(path = "/{name}/links", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listLinks(
            @PathVariable("name") String pluginName) {
        EntandoPlugin entandoPlugin = getEntandoPluginOrFail(pluginName);
        List<EntandoAppPluginLink> appLinks = entandoLinkService.getPluginLinks(entandoPlugin);
        CollectionModel<EntityModel<EntandoAppPluginLink>> lcm = getLinkCollectionModel(appLinks);
        return ResponseEntity.ok(lcm);
    }



    private EntandoPlugin getEntandoPluginOrFail(String pluginName) {
        return entandoPluginService
                .findByName(pluginName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoPlugin(pluginName);
                });
    }

    private void throwExceptionIfAlreadyDeployed(EntandoPlugin entandoPlugin) {
        Optional<EntandoPlugin> alreadyDeployedPlugin = entandoPluginService
                .findByName(entandoPlugin.getMetadata().getName());
        if (alreadyDeployedPlugin.isPresent()) {
            throw BadRequestExceptionFactory.pluginAlreadyDeployed(alreadyDeployedPlugin.get());
        }
    }

    private void addCollectionLinks(CollectionModel<EntityModel<EntandoPlugin>> collection) {
        collection.add(linkTo(methodOn(this.getClass()).get(null)).withRel("plugin"));
        collection.add(linkTo(methodOn(this.getClass()).listInNamespace(null)).withRel("plugins-in-namespace"));
        collection.add(linkTo(methodOn(this.getClass()).listLinks(null)).withRel("plugin-links"));
    }

    private CollectionModel<EntityModel<EntandoPlugin>> getPluginCollectionModel(List<EntandoPlugin> plugins) {
        return new CollectionModel<>(plugins.stream().map(resourceAssembler::toModel).collect(Collectors.toList()));
    }

    private CollectionModel<EntityModel<EntandoAppPluginLink>> getLinkCollectionModel(List<EntandoAppPluginLink> appLinks) {
        return new CollectionModel<>(appLinks.stream()
                .map(linkResourceAssembler::toModel)
                .collect(Collectors.toList()));
    }

}
