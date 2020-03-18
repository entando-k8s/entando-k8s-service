package org.entando.kubernetes.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/app-plugin-links")
public class EntandoLinksController {

    private final EntandoLinkService entandoLinkService;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> list() {
        CollectionModel<EntityModel<EntandoAppPluginLink>> cm = new CollectionModel(entandoLinkService.getAll()
                .stream().map(linkResourceAssembler::toModel).collect(
                Collectors.toList()));
        cm.add(getCollectionLinks());
        return ResponseEntity.ok(cm);
    }

    @GetMapping(params = "namespace")
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listByNamespace(@RequestParam("namespace") String namespace) {
        CollectionModel<EntityModel<EntandoAppPluginLink>> cm = new CollectionModel(entandoLinkService.getAllInNamespace(namespace)
                .stream().map(linkResourceAssembler::toModel).collect(
                        Collectors.toList()));
        cm.add(getCollectionLinks());
        return ResponseEntity.ok(cm);
    }

    @GetMapping(params = "app")
    public ModelAndView listByApp(@RequestParam("app") String appName) {
        return new ModelAndView(String.format("redirect:/apps/%s/links", appName));
    }

    @GetMapping(params = "plugin")
    public ModelAndView listByPlugin(@RequestParam("plugin") String pluginName) {
        return new ModelAndView(String.format("redirect:/plugins/%s/links", pluginName));
    }

    @GetMapping("/{name}")
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> get(@PathVariable String name) {
        EntandoAppPluginLink link = getLinkOrFail(name);
        return ResponseEntity.ok(linkResourceAssembler.toModel(link));
    }

    private EntandoAppPluginLink getLinkOrFail(String name) {
        return entandoLinkService.findByName(name)
                .orElseThrow(() -> NotFoundExceptionFactory.entandoLinkWithName(name));
    }

    private Links getCollectionLinks() {
        return Links.of(
                linkTo(methodOn(EntandoLinksController.class).get(null)).withRel("link"),
                linkTo(methodOn(EntandoAppController.class).listLinks(null)).withRel("app-links"),
                linkTo(methodOn(EntandoPluginController.class).listLinks(null)).withRel("plugin-links"),
                linkTo(methodOn(EntandoLinksController.class).listByNamespace(null)).withRel("links-in-namespace")
        );
    }

}
