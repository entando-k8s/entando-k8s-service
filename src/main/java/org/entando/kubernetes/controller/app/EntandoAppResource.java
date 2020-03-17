package org.entando.kubernetes.controller.app;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
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

@OpenAPIDefinition(tags = {@Tag(name = "entando-k8s-service"), @Tag(name = "entando-apps")})
@RequestMapping("/apps")
public interface EntandoAppResource {

    @Operation(description = "Returns apps in observed namespaces")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> list();

    @Operation(description = "Returns apps from specific namespace")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = "namespace")
    ResponseEntity<CollectionModel<EntityModel<EntandoApp>>> listInNamespace(@RequestParam String namespace);

    @Operation(description = "Find app by name in observed namespaces")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "EntandoApp not found")
    })
    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<EntityModel<EntandoApp>> get(@PathVariable("name") String appName);

    @Operation(description = "Get links associated with an EntandoApp")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "EntandoApp not found")
    })
    @GetMapping(path = "/{name}/links", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<CollectionModel<EntityModel<EntandoAppPluginLink>>> listLinks(
            @PathVariable("name") String appName);

    @Operation(description = "Add a link between an EntandoApp and an EntandoPlugin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "EntandoApp not found"),
            @ApiResponse(responseCode = "404", description = "EntandoPlugin not found")
    })
    @PostMapping(path = "/{name}/links",
            consumes = APPLICATION_JSON_VALUE,
            produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<EntityModel<EntandoAppPluginLink>> linkToPlugin(
            @PathVariable("name") String appName,
            @RequestBody EntandoPlugin entandoPlugin);

    @Operation(description = "Delete a link between an EntandoApp and an EntandoPlugin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "EntandoApp not found"),
            @ApiResponse(responseCode = "404", description = "EntandoAppPluginLink between app and plugin not found")
    })
    @DeleteMapping(path = "/{name}/links/{pluginName}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<Void> delete(
            @PathVariable("name") String appName,
            @PathVariable("pluginName") String pluginName);
}
