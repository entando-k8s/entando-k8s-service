package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@OpenAPIDefinition(tags = {@Tag(name = "entando-k8s-service")})
public interface ApiEntryPointResource {

    @Operation(description = "Returns links to entando-k8s-service endpoints")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            links = {
                    @Link(name = "apps", description = "List all the apps available in all observed namespaces"),
                    @Link(name = "plugins", description = "List all the plugins available in all observed namespaces"),
                    @Link(name = "bundles", description = "List all the bundles available in all observed namespaces"),
                    @Link(name = "namespaces", description = "List all the observed namespaces")
            }
    )
    @GetMapping(value = "/", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<CollectionModel<Object>> root();
}
