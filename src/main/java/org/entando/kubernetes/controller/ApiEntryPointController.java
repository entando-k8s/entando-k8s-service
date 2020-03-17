package org.entando.kubernetes.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collections;
import org.entando.kubernetes.controller.app.EntandoAppController;
import org.entando.kubernetes.controller.bundle.EntandoDeBundleController;
import org.entando.kubernetes.controller.namespace.KubernetesNamespaceController;
import org.entando.kubernetes.controller.plugin.EntandoPluginController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiEntryPointController implements ApiEntryPointResource {

   @Override
   public ResponseEntity<CollectionModel<Object>> root() {
       CollectionModel<Object> cm = new CollectionModel<>(Collections.emptyList());
       cm.add(linkTo(methodOn(EntandoAppController.class).list()).withRel("apps"));
       cm.add(linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"));
       cm.add(linkTo(methodOn(EntandoDeBundleController.class).list()).withRel("bundles"));
       cm.add(linkTo(methodOn(KubernetesNamespaceController.class).list()).withRel("observed-namespaces"));
       return  ResponseEntity.ok(cm);
   }

}
