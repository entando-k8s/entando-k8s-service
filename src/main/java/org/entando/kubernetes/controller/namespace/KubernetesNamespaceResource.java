package org.entando.kubernetes.controller.namespace;

import io.fabric8.kubernetes.api.model.Namespace;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletResponse;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@OpenAPIDefinition(tags = {@Tag(name = "entando-k8s-service"), @Tag(name = "observed-namespaces")})
@RequestMapping("/namespaces")
public interface KubernetesNamespaceResource {

    @Operation(description = "List all observed namespaces")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            links = {
                    @Link(name = "apps", description = "List all the EntandoApps in the observed namespaces"),
                    @Link(name = "plugins", description = "List all the EntandoPlugins in the observed namespaces"),
                    @Link(name = "bundles", description = "List all the EntandoDeBundles in the observed namespaces"),
            }
    )
    ResponseEntity<CollectionModel<EntityModel<Namespace>>> list();

    @Operation(description = "Get observed namespace")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Namespace is not part of observed namespaces"),
            @ApiResponse(responseCode = "404", description = "Observed namespace doesn't exist"),
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    links = {
                            @Link(name = "apps", description = "List all the EntandoApps in the observed namespaces"),
                            @Link(name = "plugins", description = "List all the EntandoPlugins in the observed namespaces"),
                            @Link(name = "bundles", description = "List all the EntandoDeBundles in the observed namespaces"),
                    }
            )

    })
    @GetMapping("/{name}")
    ResponseEntity<EntityModel<Namespace>> getByName(@PathVariable("name") String name);

    @GetMapping("/{name}/plugins")
    void listPluginsInNamespace(@PathVariable("name") String name, HttpServletResponse resp);

    @GetMapping("/{name}/apps")
    void listAppsInNamespace(@PathVariable("name") String name, HttpServletResponse resp);

    @GetMapping("/{name}/bundles")
    void listBundlesInNamespace(@PathVariable("name") String name, HttpServletResponse resp);
}
