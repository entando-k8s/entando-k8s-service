package org.entando.kubernetes.model.namespace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.OperatorDeploymentType;

@Slf4j
@Getter
public class ObservedNamespaces {

    private final List<ObservedNamespace> list;
    private final KubernetesUtils kubernetesUtils;
    private final List<String> names;
    private final boolean clusterScoped;

    public ObservedNamespaces(KubernetesUtils kubernetesUtils, List<String> list, OperatorDeploymentType operatorDeploymentType) {
        this.kubernetesUtils = kubernetesUtils;
        clusterScoped = operatorDeploymentType.isClusterScoped(list);
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

    public List<ObservedNamespace> getList() {
        return list;
    }

    public String getCurrentNamespace() {
        return kubernetesUtils.getCurrentNamespace();
    }

    public boolean isObservedNamespace(String namespace) {
        return isClusterScoped() || getList().stream().anyMatch(ns -> ns.getName().equals(namespace));
    }

    public void failIfNotObserved(String namespace) {
        if (namespace == null || !isObservedNamespace(namespace)) {
            throw new NotObservedNamespaceException(namespace);
        }
    }

    public boolean isClusterScoped() {
        return this.clusterScoped;
    }
}
