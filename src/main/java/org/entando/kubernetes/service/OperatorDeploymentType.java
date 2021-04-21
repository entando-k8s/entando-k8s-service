package org.entando.kubernetes.service;

import java.util.List;

public enum OperatorDeploymentType {
    OLM {
        @Override
        public boolean isClusterScoped(List<String> namespaces) {
            return namespaces == null || namespaces.isEmpty() || namespaces.stream().anyMatch(String::isEmpty);
        }
    },
    HELM {
        @Override
        public boolean isClusterScoped(List<String> namespaces) {
            return namespaces != null && namespaces.stream().anyMatch("*"::equals);
        }
    };

    public abstract boolean isClusterScoped(List<String> namespaces);
}
