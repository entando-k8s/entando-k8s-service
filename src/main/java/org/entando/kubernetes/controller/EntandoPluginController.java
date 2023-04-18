package org.entando.kubernetes.controller;

import static java.util.Optional.ofNullable;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.IngressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.zalando.problem.ThrowableProblem;


@Slf4j
@RestController
@RequestMapping("/plugins")
@RequiredArgsConstructor
public class EntandoPluginController {

    private final EntandoLinkService linkService;
    private final EntandoPluginService pluginService;
    private final IngressService ingressService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoPlugin>> list() {
        log.info("Listing all deployed plugins in observed namespaces");
        List<EntandoPlugin> plugins = pluginService.getAll();
        return ResponseEntity.ok(plugins);
    }


    @GetMapping(produces = {APPLICATION_JSON_VALUE}, params = "namespace")
    public ResponseEntity<List<EntandoPlugin>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing all deployed plugins in {} observed namespace", namespace);
        List<EntandoPlugin> plugins = pluginService.getAllInNamespace(namespace);
        return ResponseEntity.ok(plugins);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoPlugin> get(@PathVariable("name") final String pluginName,
            @RequestParam(value = "namespace", required = false) String namespace) {
        log.info("Searching plugin with name {} in observed namespaces", pluginName);

        if (StringUtils.isEmpty(namespace)) {
            log.info("Searching plugin with name {} in observed namespaces", pluginName);
        } else {
            log.info("Searching plugin with name {} in namespace {}", pluginName, namespace);
        }

        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName, namespace);
        return ResponseEntity.ok(plugin);
    }

    @GetMapping(path = "/{name}/ingress", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<Ingress> getPluginIngress(@PathVariable("name") final String pluginName,
            @RequestParam(value = "namespace", required = false) String namespace) {

        if (StringUtils.isEmpty(namespace)) {
            log.info("Searching plugin ingress with name {} in observed namespaces", pluginName);
        } else {
            log.info("Searching plugin ingress with name {} in namespace {}", pluginName, namespace);
        }

        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName, namespace);
        Ingress pluginIngress = getEntandoPluginIngressOrFail(plugin);
        return ResponseEntity.ok(pluginIngress);
    }

    @DeleteMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> delete(@PathVariable("name") String pluginName,
            @RequestParam(value = "namespace", required = false) String namespace) {

        if (StringUtils.isEmpty(namespace)) {
            log.info("Deleting plugin with identifier {} from observed namespaces", pluginName);
        } else {
            log.info("Deleting plugin with identifier {} from namespace {}", pluginName, namespace);
        }

        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName, namespace);
        pluginService.deletePlugin(plugin);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(path = "/ingress/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> deletePluginIngressPath(@PathVariable("name") String pluginName,
            @RequestParam(value = "namespace", required = false) String namespace) {

        if (StringUtils.isEmpty(namespace)) {
            log.info("Deleting ingress path for plugin with identifier {} from observed namespaces", pluginName);
        } else {
            log.info("Deleting ingress path for plugin with identifier {} from namespace {}", pluginName, namespace);
        }

        EntandoPlugin plugin = getEntandoPluginOrFail(pluginName, namespace);
        List<EntandoAppPluginLink> links = linkService.getPluginLinks(plugin);
        ingressService.deletePathFromIngressByEntandoPlugin(plugin, links);

        return ResponseEntity.accepted().build();
    }


    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoPlugin> create(
            @RequestBody EntandoPlugin entandoPlugin) {

        throwExceptionIfAlreadyDeployed(entandoPlugin);
        return this.excuteCreateOrReplacePlugin(entandoPlugin, EntandoPluginService.CREATE);
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoPlugin> createOrReplace(
            @RequestBody EntandoPlugin entandoPlugin) {

        return this.excuteCreateOrReplacePlugin(entandoPlugin, EntandoPluginService.CREATE_OR_REPLACE);
    }

    private ResponseEntity<EntandoPlugin> excuteCreateOrReplacePlugin(EntandoPlugin entandoPlugin,
            boolean createOrReplace) {

        EntandoPlugin deployedPlugin = pluginService.deploy(entandoPlugin, createOrReplace);
        URI resourceLink = MvcUriComponentsBuilder.fromMethodCall(
                on(getClass()).get(deployedPlugin.getMetadata().getName(),
                        deployedPlugin.getMetadata().getNamespace())).build().toUri();
        return ResponseEntity.created(resourceLink).body(deployedPlugin);
    }

    private EntandoPlugin getEntandoPluginOrFail(String pluginName, String namespace) {

        return ofNullable(namespace)
                .flatMap(ns -> pluginService.findByNameAndNamespace(pluginName, ns))
                .or(() -> pluginService.findByName(pluginName))
                .orElseThrow(() -> NotFoundExceptionFactory.entandoPlugin(pluginName));
    }

    private Ingress getEntandoPluginIngressOrFail(EntandoPlugin plugin) {
        return ingressService
                .findByEntandoPlugin(plugin)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.ingress(plugin);
                });

    }

    private void throwExceptionIfAlreadyDeployed(EntandoPlugin entandoPlugin) {
        Optional<EntandoPlugin> alreadyDeployedPlugin = pluginService
                .findByName(entandoPlugin.getMetadata().getName());
        if (alreadyDeployedPlugin.isPresent()) {
            throw BadRequestExceptionFactory.pluginAlreadyDeployed(alreadyDeployedPlugin.get());
        }
    }


}
