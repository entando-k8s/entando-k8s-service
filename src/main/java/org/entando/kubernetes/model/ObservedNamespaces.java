package org.entando.kubernetes.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.entando.kubernetes.service.KubernetesUtils;

@Getter
public class ObservedNamespaces {

    private final List<String> nsList;
    private final KubernetesUtils kubernetesUtils;

    public ObservedNamespaces(List<String> nsList) {
        this(new KubernetesUtils(), nsList);
    }

    public ObservedNamespaces(KubernetesUtils kubernetesUtils) {
        this(kubernetesUtils, new ArrayList<>());
    }

    public ObservedNamespaces(KubernetesUtils kubernetesUtils, List<String> list) {
        this.nsList = new ArrayList<>();
        if (list != null) {
            this.nsList.addAll(list);
        }
        this.kubernetesUtils = kubernetesUtils;
        String currentNamespace = this.kubernetesUtils.getCurrentNamespace();
        if (!nsList.contains(currentNamespace)) {
            nsList.add(currentNamespace);
        }
    }

    public String getCurrentNamespace() {
        return kubernetesUtils.getCurrentNamespace();
    }

    public boolean isObservedNamespace(String namespace) {
        return nsList.contains(namespace);
    }

    public void failIfNotObserved(String namespace) {
        if (namespace == null || !isObservedNamespace(namespace)) {
            throw new NotObservedNamespaceException(namespace);
        }
    }

}
