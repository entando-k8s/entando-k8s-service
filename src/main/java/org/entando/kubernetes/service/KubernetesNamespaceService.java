package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.entando.kubernetes.model.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KubernetesNamespaceService {

    private final KubernetesClient client;
    private final ObservedNamespaces observedNamespaces;

    public List<Namespace> getObservedNamespaceList() {
        return this.client.namespaces().list().getItems().stream()
                .filter(ns -> observedNamespaces.isObservedNamespace(ns.getMetadata().getName()))
                .collect(Collectors.toList());
    }

    public Optional<Namespace> getObservedNamespace(String name) {
        return getObservedNamespaceList().stream().filter(ns -> ns.getMetadata().getName().equals(name)).findAny();
    }

}
