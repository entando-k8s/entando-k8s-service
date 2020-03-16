package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {

    private final KubernetesClient client;
    private final List<String> observedNamespaces;

    public NamespaceService(KubernetesClient client, List<String> observedNamespaces) {
        this.client = client;
        this.observedNamespaces = observedNamespaces;

    }

    public List<Namespace> getObservedNamespaceList() {
        return this.client.namespaces().list().getItems().stream()
                .filter(ns -> observedNamespaces.contains(ns.getMetadata().getName()))
                .collect(Collectors.toList());
    }

    public Optional<Namespace> getObservedNamespace(String name) {
        return getObservedNamespaceList().stream().filter(ns -> ns.getMetadata().getName().equals(name)).findAny();
    }

}
