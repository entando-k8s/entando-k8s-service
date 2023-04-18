package org.entando.kubernetes.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.net.URI;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/app-plugin-links")
public class EntandoLinksController {

    private final EntandoLinkService linkService;
    private final EntandoAppService appService;
    private final EntandoPluginService pluginService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoAppPluginLink>> list() {
        return ResponseEntity.ok(linkService.getAll());
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE}, params = "namespace")
    public ResponseEntity<List<EntandoAppPluginLink>> listInNamespace(@RequestParam("namespace") String namespace) {
        List<EntandoAppPluginLink> el = linkService.getAllInNamespace(namespace);
        return ResponseEntity.ok(el);
    }

    @GetMapping(params = "app", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoAppPluginLink>> listAppLinks(@RequestParam("app") String appName) {
        List<EntandoAppPluginLink> el = linkService.findByAppName(appName);
        return ResponseEntity.ok(el);
    }

    @GetMapping(params = "plugin", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoAppPluginLink>> listPluginLinks(@RequestParam("plugin") String pluginName) {
        List<EntandoAppPluginLink> el = linkService.findByPluginName(pluginName);
        return ResponseEntity.ok(el);
    }

    @GetMapping(value = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoAppPluginLink> get(@PathVariable String name) {
        EntandoAppPluginLink link = getLinkByNameOrFail(name);
        return ResponseEntity.ok(link);
    }

    @PostMapping(consumes = {APPLICATION_JSON_VALUE}, produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoAppPluginLink> create(@RequestBody AppPluginLinkRequest req) {
        EntandoApp ea = getAppByNameOrFail(req.getAppName());
        EntandoPlugin ep = getPluginByNameOrFail(req.getPluginName());
        EntandoAppPluginLink link = linkService.buildBetweenAppAndPlugin(ea, ep);
        URI linkLocation = MvcUriComponentsBuilder.fromMethodCall(on(getClass()).get(link.getMetadata().getName()))
                .build().toUri();

        assert linkLocation != null;
        return ResponseEntity.created(linkLocation).body(link);
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

}
