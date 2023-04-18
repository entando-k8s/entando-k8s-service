package org.entando.kubernetes.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoDeploymentPhase;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.IngressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
@RequestMapping("/apps")
public class EntandoAppController {

    private static final String UNDEFINED = "undefined";
    private final EntandoAppService appService;
    private final ObservedNamespaces observedNamespaces;
    private final EntandoLinkService linkService;
    private final IngressService ingressService;
    private final EntandoPluginService pluginService;

    @GetMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<EntandoApp>> list() {
        log.info("Listing apps from all observed namespaces");
        List<EntandoApp> entandoApps = appService.getAll();
        return ResponseEntity.ok(entandoApps);
    }


    @GetMapping(produces = {APPLICATION_JSON_VALUE}, params = "namespace")
    public ResponseEntity<List<EntandoApp>> listInNamespace(@RequestParam String namespace) {
        log.info("Listing apps");
        List<EntandoApp> entandoApps = appService.getAllInNamespace(namespace);
        return ResponseEntity.ok(entandoApps);
    }

    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoApp> get(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        return ResponseEntity.ok(entandoApp);
    }

    @GetMapping(path = "/{name}/ingress", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<Ingress> getAppIngress(@PathVariable("name") String appName) {
        log.debug("Requesting app with name {}", appName);
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        Ingress appIngress = getEntandoAppIngressOrFail(entandoApp);
        return ResponseEntity.ok(appIngress);
    }

    @PostMapping(path = "/{name}/links", consumes = APPLICATION_JSON_VALUE, produces = {
            APPLICATION_JSON_VALUE})
    public ResponseEntity<EntandoAppPluginLink> linkToPlugin(
            @PathVariable("name") String appName,
            @RequestBody EntandoPlugin entandoPlugin) {
        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        EntandoPlugin plugin = createPlugin(entandoPlugin);
        EntandoAppPluginLink newLink = linkService.buildBetweenAppAndPlugin(entandoApp, plugin);
        EntandoAppPluginLink deployedLink = linkService.deploy(newLink);
        return ResponseEntity.status(HttpStatus.CREATED).body(deployedLink);
    }

    @Operation(description = "Returns application installation status")
    @ApiResponse(responseCode = "200", description = "OK with status phase, used 'undefined' for error")
    @ApiResponse(responseCode = "404", description = "Application by name not found")
    @GetMapping(path = "/{name}/status", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<ApplicationStatus> getAppStatus(@PathVariable("name") String appName) {
        log.debug("Requesting deployment status of app with name {}", appName);
        EntandoDeploymentPhase status = null;

        EntandoApp entandoApp = getEntandoAppOrFail(appName);
        status = entandoApp.getStatus().getPhase();

        ApplicationStatus returnStatus = new ApplicationStatus();
        if (status == null) {
            returnStatus.setStatus(UNDEFINED);
        } else {
            returnStatus.setStatus(status.toValue());
        }
        return ResponseEntity.ok(returnStatus);
    }

    public Ingress getEntandoAppIngressOrFail(EntandoApp app) {
        return ingressService.findByEntandoApp(app).<ThrowableProblem>orElseThrow(() -> {
            throw NotFoundExceptionFactory.ingress(app);
        });
    }

    private EntandoApp getEntandoAppOrFail(String appName) {

        return appService
                .findByNameAndDefaultNamespace(appName)
                .<ThrowableProblem>orElseThrow(() -> {
                    throw NotFoundExceptionFactory.entandoApp(appName);
                });
    }

    private EntandoPlugin createPlugin(EntandoPlugin newPlugin) {
        String pluginNamespace = Optional.ofNullable(newPlugin.getMetadata().getNamespace())
                .filter(ns -> !ns.isEmpty())
                .orElse(observedNamespaces.getCurrentNamespace());
        newPlugin.getMetadata().setNamespace(pluginNamespace);
        return pluginService.deploy(newPlugin, EntandoPluginService.CREATE_OR_REPLACE);
    }

    @Data
    @NoArgsConstructor
    public static class ApplicationStatus {

        private String status;
    }
}
