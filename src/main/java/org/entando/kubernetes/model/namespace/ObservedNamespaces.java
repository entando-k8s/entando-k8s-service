package org.entando.kubernetes.model.namespace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.model.namespace.provider.FileBasedNamespaceProvider;
import org.entando.kubernetes.service.KubernetesUtils;

@Slf4j
@Getter
public class ObservedNamespaces {

    private final List<ObservedNamespace> list;
    private final KubernetesUtils kubernetesUtils;
    private final List<String> names;

    public ObservedNamespaces(List<String> list) {
        this(new KubernetesUtils(new FileBasedNamespaceProvider()), list);
    }

    public ObservedNamespaces(KubernetesUtils kubernetesUtils) {
        this(kubernetesUtils, new ArrayList<>());
    }

    public ObservedNamespaces(KubernetesUtils kubernetesUtils, List<String> list) {
        this.kubernetesUtils = kubernetesUtils;
        Set<String> finalList = new HashSet<>();
        if (list != null) {
            finalList.addAll(list);
        }
        if (this.getCurrentNamespace() != null) {
            finalList.add(this.getCurrentNamespace());
        }
        this.names = new ArrayList<>(finalList);
        this.list = finalList.stream().map(ObservedNamespace::new).collect(Collectors.toList());
        log.info("ObservedNamespaces are {}", String.join(", ", this.names));
    }

    public String getCurrentNamespace() {
        return kubernetesUtils.getCurrentNamespace();
    }

    public boolean isObservedNamespace(String namespace) {
        return getList().stream().anyMatch(ns -> ns.getName().equals(namespace));
    }

    public void failIfNotObserved(String namespace) {
        if (namespace == null || !isObservedNamespace(namespace)) {
            throw new NotObservedNamespaceException(namespace);
        }
    }
    
}
