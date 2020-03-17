package org.entando.kubernetes.controller.bundle;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@OpenAPIDefinition(tags = {@Tag(name = "entando-k8s-service"), @Tag(name = "digital-exchange"), @Tag(name="entando-de-bundles")})
@RequestMapping("/bundles")
public interface EntandoDeBundleResource {

    @Operation(description = "List all available EntandoDeBundles in the observed namespaces")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(path = "", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> list();

    @Operation(description = "List EntandoDeBundle in a specific namespace")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE}, params = {"namespace"})
    ResponseEntity<CollectionModel<EntityModel<EntandoDeBundle>>> listInNamespace(
            @RequestParam("namespace") String namespace);

    @Operation(description = "Find EntandoDeBundle by name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "EntandoDeBundle not found in observed namespaces")
    })
    @GetMapping(path = "/{name}", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
    ResponseEntity<EntityModel<EntandoDeBundle>> get(@PathVariable("name") String name);
}
