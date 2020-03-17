package org.entando.kubernetes.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.service.KubernetesUtils;

@Getter
public class ObservedNamespaces {

    private final List<String> list;
    private final KubernetesUtils kubernetesUtils;

    public ObservedNamespaces(KubernetesUtils kubernetesUtils) {
        this(kubernetesUtils, new ArrayList<>());
    }

    public ObservedNamespaces(KubernetesUtils kubernetesUtils, List<String> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        this.list = list;
        this.kubernetesUtils = kubernetesUtils;
        String currentNamespace = this.kubernetesUtils.getCurrentNamespace();
        if (!list.contains(currentNamespace)) {
            list.add(currentNamespace);
        }
    }

    public String getCurrentNamespace() {
        return kubernetesUtils.getCurrentNamespace();
    }

    public boolean isObservedNamespace(String namespace) {
        return list.contains(namespace);
    }

    public void failIfNotObserved(String namespace) {
        if (!isObservedNamespace(namespace)) {
            throw new NotObservedNamespaceException(namespace);
        }
    }

}
