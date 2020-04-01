package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoKubernetesServiceProvider;
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
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;


@Slf4j
@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class EntandoPluginController {

    private final EntandoKubernetesServiceProvider serviceProvider;
    private final EntandoPluginResourceAssembler resourceAssembler;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoPlugin>>> list() {
        log.info("Listing all deployed plugins in observed namespaces");
        List<EntandoPlugin> plugins = serviceProvider.getPluginService().getAll();
        CollectionModel<EntityModel<EntandoPlugin>> collection = getPluginCollectionModel(plugins);
        addCollectionLinks(collection);
        return ResponseEntity.ok(collection);
    }


    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoPlugin>>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing all deployed plugins in {} observed namespace", namespace);
        List<EntandoPlugin> plugins = serviceProvider.getPluginService().getAllInNamespace(namespace);
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

    @GetMapping(path = "/{name}/ingress", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<Ingress>> getPluginIngress(@PathVariable("name") final String pluginName) {
        log.info("Searching plugin with name {} in observed namespaces", pluginName);
        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName);
        Ingress pluginIngress = getEntandoPluginIngressOrFail(plugin);
        return ResponseEntity.ok(new EntityModel<>(pluginIngress));
    }

    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable("name") String pluginName) {
        log.info("Deleting plugin with identifier {} from observed namespaces", pluginName);
        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName);
        serviceProvider.getPluginService().deletePlugin(plugin);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoPlugin>> create(
            @RequestBody EntandoPlugin entandoPlugin) {
        throwExceptionIfAlreadyDeployed(entandoPlugin);
        EntandoPlugin deployedPlugin = serviceProvider.getPluginService().deploy(entandoPlugin);
        URI resourceLink = linkTo(methodOn(getClass()).get(deployedPlugin.getMetadata().getName())).toUri();
        return ResponseEntity.created(resourceLink).body(resourceAssembler.toModel(deployedPlugin));
    }

    private EntandoPlugin getEntandoPluginOrFail(String pluginName) {
        return serviceProvider.getPluginService()
                .findByName(pluginName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoPlugin(pluginName);
                });
    }

    private Ingress getEntandoPluginIngressOrFail(EntandoPlugin plugin) {
        return serviceProvider.getIngressService()
                .findByEntandoPlugin(plugin)
                .orElseThrow(() -> {
                    throw notFoundIngressForPlugin(plugin);
                });

    }

    private void throwExceptionIfAlreadyDeployed(EntandoPlugin entandoPlugin) {
        Optional<EntandoPlugin> alreadyDeployedPlugin = serviceProvider.getPluginService()
                .findByName(entandoPlugin.getMetadata().getName());
        if (alreadyDeployedPlugin.isPresent()) {
            throw BadRequestExceptionFactory.pluginAlreadyDeployed(alreadyDeployedPlugin.get());
        }
    }

    private void addCollectionLinks(CollectionModel<EntityModel<EntandoPlugin>> collection) {
        collection.add(linkTo(methodOn(EntandoPluginController.class).get(null)).withRel("plugin"));
        collection.add(linkTo(methodOn(EntandoPluginController.class).listInNamespace(null)).withRel("plugins-in-namespace"));
        collection.add(linkTo(methodOn(EntandoLinksController.class).listPluginLinks(null)).withRel("plugin-links"));
    }

    private CollectionModel<EntityModel<EntandoPlugin>> getPluginCollectionModel(List<EntandoPlugin> plugins) {
        return new CollectionModel<>(plugins.stream().map(resourceAssembler::toModel).collect(Collectors.toList()));
    }

    private ThrowableProblem notFoundIngressForPlugin(EntandoPlugin plugin) {
        return Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .withDetail("Ingress not found for EntandoPlugin " + plugin.getMetadata().getName() +
                        " in namespace " + plugin.getMetadata().getNamespace())
                .build();
    }


}
