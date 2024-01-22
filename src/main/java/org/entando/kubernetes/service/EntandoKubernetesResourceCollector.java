package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.PluginVariable;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.model.plugin.EntandoPlugin;

@Slf4j
@RequiredArgsConstructor
public abstract class EntandoKubernetesResourceCollector<T extends HasMetadata> {

    protected final KubernetesUtils kubernetesUtils;
    protected final ObservedNamespaces observedNamespaces;

    public List<T> getAll() {
        if (observedNamespaces.isClusterScoped()) {
            return getInAnyNamespace();
        } else {
            return collectFromNamespaces(observedNamespaces.getNames());
        }
    }

    public List<T> getAllInNamespace(String namespace) {
        return getInNamespaceWithoutChecking(namespace);
    }

    protected abstract List<T> getInAnyNamespace();

    protected abstract List<T> getInNamespaceWithoutChecking(String namespace);

    public List<T> collectFromNamespaces(List<String> namespaceList) {
        return namespaceList.stream().flatMap(ns -> getAllInNamespace(ns).stream()).collect(Collectors.toList());
    }

    public Optional<T> findByName(String name) {
        return getAll().stream().filter(r -> r.getMetadata().getName().equals(name)).findFirst();
    }

    public Optional<T> findByNameAndNamespace(String name, String namespace) {
        return getAllInNamespace(namespace)
                .stream()
                .filter(r -> r.getMetadata().getName().equals(name))
                .findFirst();
    }

    public Optional<T> findByNameAndDefaultNamespace(String name) {

        final String namespace = kubernetesUtils.getDefaultPluginNamespace();
        return getAllInNamespace(namespace)
                .stream()
                .filter(r -> r.getMetadata().getName().equals(name))
                .findFirst();
    }
}
