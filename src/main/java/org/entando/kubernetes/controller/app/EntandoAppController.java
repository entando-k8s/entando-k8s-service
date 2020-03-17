package org.entando.kubernetes.controller.app;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.assembler.EntandoAppPluginLinkResourceAssembler;
import org.entando.kubernetes.service.assembler.EntandoAppResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.ThrowableProblem;


@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class EntandoAppController implements EntandoAppResource {

    private final EntandoAppService entandoAppService;
    private final EntandoLinkService entandoLinkService;
    private final EntandoPluginService entandoPluginService;
    private final EntandoAppResourceAssembler appResourceAssembler;
    private final EntandoAppPluginLinkResourceAssembler linkResourceAssembler;
    private final KubernetesUtils k8sUtils;

    @Override
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> list() {
        log.info("Listing apps from all observed namespaces");
        List<EntandoApp> entandoApps = entandoAppService.getAll();
        return ResponseEntity.ok(new CollectionModel<>(
                entandoApps.stream().map(appResourceAssembler::toModel).collect(Collectors.toList())));
    }


    @Override
    public ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing apps");
        List<EntandoApp> entandoApps = entandoAppService.getAllInNamespace(namespace);
        return ResponseEntity.ok(new CollectionModel<>(
                entandoApps.stream().map(appResourceAssembler::toModel).collect(Collectors.toList())));
    }

    @Override
    public ResponseEntity<EntityModel<EntandoApp>> get(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        return ResponseEntity.ok(appResourceAssembler.toModel(entandoApp));
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listLinks(
            @PathVariable("name") String appName) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        List<EntandoAppPluginLink> appLinks = entandoLinkService.getAppLinks(entandoApp);
        List<EntityModel<EntandoAppPluginLink>> linkResources = appLinks.stream()
                .map(linkResourceAssembler::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new CollectionModel<>(linkResources));
    }

    @Override
    public ResponseEntity<EntityModel<EntandoAppPluginLink>> linkToPlugin(
            @PathVariable("name") String appName,
            @RequestBody EntandoPlugin entandoPlugin) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        EntandoPlugin plugin = getOrCreatePlugin(entandoPlugin);
        EntandoAppPluginLink newLink = entandoLinkService.buildBetweenAppAndPlugin(entandoApp, plugin);
        EntandoAppPluginLink deployedLink = entandoLinkService.deploy(newLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkResourceAssembler.toModel(deployedLink));
    }

    @Override
    public ResponseEntity<Void> delete(
            @PathVariable("name") String appName,
            @PathVariable("pluginName") String pluginName) {
        EntandoApp app = getEntandoAppOrFail(appName);
        EntandoAppPluginLink linkToRemove = getLinkOrFail(app, pluginName);
        entandoLinkService.delete(linkToRemove);
        return ResponseEntity.accepted().build();
    }

    private EntandoApp getEntandoAppOrFail(String appName) {
        return entandoAppService
                .findByName(appName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoApp(appName);
                });
    }

    private EntandoAppPluginLink getLinkOrFail(EntandoApp app, String pluginName) {
        return entandoLinkService.getLink(app, pluginName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoLink(app.getMetadata().getName(), pluginName);
                });
    }

    private EntandoPlugin getOrCreatePlugin(EntandoPlugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        Optional<EntandoPlugin> optionalPlugin = entandoPluginService.findByName(pluginName);
        return optionalPlugin.orElseGet(() -> {
            String pluginNamespace = Optional.ofNullable(plugin.getMetadata().getNamespace())
                    .filter(ns -> !ns.isEmpty())
                    .orElse(k8sUtils.getCurrentNamespace());
            plugin.getMetadata().setNamespace(pluginNamespace);
            return entandoPluginService.deploy(plugin);
        });

    }

}
