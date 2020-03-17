package org.entando.kubernetes.controller.namespace;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.fabric8.kubernetes.api.model.Namespace;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.entando.kubernetes.controller.app.EntandoAppController;
import org.entando.kubernetes.controller.bundle.EntandoDeBundleController;
import org.entando.kubernetes.controller.plugin.EntandoPluginController;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.service.KubernetesNamespaceService;
import org.entando.kubernetes.service.assembler.KubernetesNamespaceResourceAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubernetesNamespaceController implements KubernetesNamespaceResource {

    private final KubernetesNamespaceResourceAssembler nsResourceAssembler;
    private final KubernetesNamespaceService nsService;

    public KubernetesNamespaceController(KubernetesNamespaceResourceAssembler nrs, KubernetesNamespaceService nsService) {
       this.nsResourceAssembler = nrs;
       this.nsService = nsService;
    }

    @Override
    public ResponseEntity<CollectionModel<EntityModel<Namespace>>> list() {
        List<Namespace> observedNamespaces = nsService.getObservedNamespaceList();
        CollectionModel<EntityModel<Namespace>> nsCollection = new CollectionModel<>(
                observedNamespaces.stream()
                        .map(nsResourceAssembler::toModel)
                        .collect(Collectors.toList()));
        nsCollection.add(linkTo(methodOn(EntandoAppController.class).list()).withRel("apps"));
        nsCollection.add(linkTo(methodOn(EntandoPluginController.class).list()).withRel("plugins"));
        nsCollection.add(linkTo(methodOn(EntandoDeBundleController.class).list()).withRel("bundles"));
        return ResponseEntity.ok(nsCollection);
    }

    @Override
    @GetMapping("/{name}")
    public ResponseEntity<EntityModel<Namespace>> getByName(@PathVariable("name") String name) {
        Optional<Namespace> observedNs = nsService.getObservedNamespace(name);
        if (!observedNs.isPresent()) {
            throw NotFoundExceptionFactory.observedNamespace(name);
        }
        EntityModel<Namespace> ns = nsResourceAssembler.toModel(observedNs.get());
        return ResponseEntity.ok(ns);
    }

    @Override
    @GetMapping("/{name}/plugins")
    public void listPluginsInNamespace(@PathVariable("name") String name, HttpServletResponse resp) {
        String validNamespace = validateNamespace(name);
        resp.setHeader("Location", "/plugins?namespace="+validNamespace);
        resp.setStatus(302);
    }

    @Override
    @GetMapping("/{name}/apps")
    public void listAppsInNamespace(@PathVariable("name") String name, HttpServletResponse resp) {
        String validNamespace = validateNamespace(name);
        resp.setHeader("Location", "/apps?namespace=" + validNamespace);
        resp.setStatus(302);
    }

    @Override
    @GetMapping("/{name}/bundles")
    public void listBundlesInNamespace(@PathVariable("name") String name, HttpServletResponse resp) {
        String validNamespace = validateNamespace(name);
        resp.setHeader("Location", "/bundles?namespace=" + validNamespace);
        resp.setStatus(302);
    }

    public String validateNamespace(String namespace) {
        if (!namespace.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?")) {
            throw BadRequestExceptionFactory.invalidNamespace(namespace);
        }
        return namespace;
    }
}
