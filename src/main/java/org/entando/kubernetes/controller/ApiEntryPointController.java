package org.entando.kubernetes.controller;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Collections;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiEntryPointController {

   @GetMapping(value = "/", produces = {APPLICATION_JSON_VALUE, HAL_JSON_VALUE})
   public ResponseEntity<CollectionModel<Object>> root() {
       CollectionModel<Object> cm = new CollectionModel<>(Collections.emptyList());
       cm.add(linkTo(methodOn(EntandoAppController.class).list()).withRel("apps"));
       cm.add(linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"));
       cm.add(linkTo(methodOn(EntandoDeBundleController.class).list()).withRel("bundles"));
       cm.add(linkTo(methodOn(EntandoLinksController.class).list()).withRel("app-plugin-links"));
       cm.add(linkTo(methodOn(ObservedNamespaceController.class).list()).withRel("observed-namespaces"));
       return  ResponseEntity.ok(cm);
   }

}
