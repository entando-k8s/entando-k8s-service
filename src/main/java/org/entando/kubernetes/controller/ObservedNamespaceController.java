package org.entando.kubernetes.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespace;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/namespaces")
@RequiredArgsConstructor
public class ObservedNamespaceController {

    private final ObservedNamespaces observedNamespaces;

    @GetMapping(produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ObservedNamespace>> list() {
        return ResponseEntity.ok(observedNamespaces.getList());
    }

    @GetMapping(value = "/{name}", produces = {APPLICATION_JSON_VALUE})
    public ResponseEntity<ObservedNamespace> getByName(@PathVariable String name) {
        String validNamespace = validateNamespace(name);
        return ResponseEntity.ok(new ObservedNamespace(validNamespace));
    }

    public String validateNamespace(String namespace) {
        if (!namespace.matches("[a-z0-9]([-a-z0-9]*[a-z0-9])?")) {
            throw BadRequestExceptionFactory.invalidNamespace(namespace);
        }
        return namespace;
    }

}
