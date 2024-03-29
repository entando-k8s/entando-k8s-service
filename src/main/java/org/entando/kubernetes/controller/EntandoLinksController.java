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
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoDeploymentPhase;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.request.AppPluginLinkRequest;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/app-plugin-links")
public class EntandoLinksController {

    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;
    private final EntandoLinkService linkService;
    private final EntandoAppService appService;
    private final EntandoPluginService pluginService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> list() {
        return ResponseEntity.ok(getCollectionWithLinks(linkService.getAll()));
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listInNamespace(@RequestParam("namespace") String namespace) {
        List<EntandoAppPluginLink> el = linkService.getAllInNamespace(namespace);
        return ResponseEntity.ok(getCollectionWithLinks(el));
    }

    @GetMapping(params = "app", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listAppLinks(@RequestParam("app") String appName) {
        List<EntandoAppPluginLink> el = linkService.findByAppName(appName);
        return ResponseEntity.ok(getCollectionWithLinks(el));
    }

    @GetMapping(params = "plugin", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listPluginLinks(@RequestParam("plugin") String pluginName) {
        List<EntandoAppPluginLink> el = linkService.findByPluginName(pluginName);
        return ResponseEntity.ok(getCollectionWithLinks(el));
    }

    @GetMapping(value = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> get(@PathVariable String name) {
        EntandoAppPluginLink link = getLinkByNameOrFail(name);
        return ResponseEntity.ok(linkResourceAssembler.toModel(link));
    }

    @PostMapping(consumes = {APPLICATION_JSON_VALUE}, produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> create(@RequestBody AppPluginLinkRequest req) {
        EntandoApp ea = getAppByNameOrFail(req.getAppName());
        EntandoPlugin ep = getPluginByNameOrFail(req.getPluginName());
        EntandoAppPluginLink link = linkService.buildBetweenAppAndPlugin(ea, ep);
        EntityModel<EntandoAppPluginLink> model = linkResourceAssembler.toModel(link);
        URI linkLocation = model.getLink(IanaLinkRelations.SELF).map(Link::toUri).orElse(null);
        assert linkLocation != null;
        return ResponseEntity.created(linkLocation).body(model);
    }

    @DeleteMapping(value = "/{name}")
    public ResponseEntity<Object> delete(@PathVariable String name) {
        EntandoAppPluginLink link = getLinkByNameOrFail(name);
        linkService.delete(link);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping(value = "/delete-and-scale-down/{name}")
    public ResponseEntity<Object> deleteAndScaleDown(@PathVariable String name) {
        EntandoAppPluginLink link = getLinkByNameOrFail(name);
        disableActivePlugin(link);
        linkService.delete(link);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    private void disableActivePlugin(EntandoAppPluginLink link) {
        Optional<EntandoPlugin> optPlugin = pluginService.findByName(link.getSpec().getEntandoPluginName());
        if (optPlugin.isPresent() && optPlugin.get().getStatus().getPhase().equals(EntandoDeploymentPhase.SUCCESSFUL)) {
            log.info("Scalind down plugin {} as it's deployment phase is SUCCESSFUL",
                    optPlugin.get().getMetadata().getName());
            pluginService.scaleDownPlugin(optPlugin.get());
        }
    }

    private EntandoApp getAppByNameOrFail(String name) {
        return appService.findByName(name)
                .orElseThrow(() -> NotFoundExceptionFactory.entandoApp(name));
    }

    private EntandoPlugin getPluginByNameOrFail(String name) {
        return pluginService.findByName(name)
                .orElseThrow(() -> NotFoundExceptionFactory.entandoPlugin(name));
    }

    private EntandoAppPluginLink getLinkByNameOrFail(String name) {
        return linkService.findByName(name)
                .orElseThrow(() -> NotFoundExceptionFactory.entandoLinkWithName(name));
    }

    private CollectionModel<EntityModel<EntandoAppPluginLink>> getCollectionWithLinks(List<EntandoAppPluginLink> all) {
        CollectionModel<EntityModel<EntandoAppPluginLink>> cm = new CollectionModel<>(all
                .stream().map(linkResourceAssembler::toModel).collect(
                        Collectors.toList()));
        cm.add(getCollectionLinks());
        return cm;
    }

    private Links getCollectionLinks() {
        return Links.of(
                linkTo(methodOn(EntandoLinksController.class).get(null)).withRel("app-plugin-link"),
                linkTo(methodOn(EntandoLinksController.class).delete(null)).withRel("delete"),
                linkTo(methodOn(EntandoLinksController.class).deleteAndScaleDown(null)).withRel("delete-and-scale-down"),
                linkTo(methodOn(EntandoLinksController.class).listAppLinks(null)).withRel("app-links"),
                linkTo(methodOn(EntandoLinksController.class).listPluginLinks(null)).withRel("plugin-links"),
                linkTo(methodOn(EntandoLinksController.class).listInNamespace(null)).withRel("app-plugin-links-in-namespace")
        );
    }
}
