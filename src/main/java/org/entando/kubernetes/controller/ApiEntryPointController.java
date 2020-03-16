package org.entando.kubernetes.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiEntryPointController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String HAL_JSON = MediaTypes.HAL_JSON_VALUE;

    @GetMapping(value = "/", produces = {JSON, HAL_JSON})
   public ResponseEntity<CollectionModel<Object>> endpoints() {
       CollectionModel<Object> cm = new CollectionModel<>(Collections.emptyList());
       cm.add(linkTo(methodOn(EntandoAppController.class).list()).withRel("apps"));
       cm.add(linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"));
       cm.add(linkTo(methodOn(EntandoDeBundleController.class).list()).withRel("bundles"));
       cm.add(linkTo(methodOn(NamespaceController.class).list()).withRel("observed-namespaces"));
       return  ResponseEntity.ok(cm);
   }

}
