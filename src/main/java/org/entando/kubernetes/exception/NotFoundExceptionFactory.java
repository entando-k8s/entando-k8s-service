package org.entando.kubernetes.exception;

import org.entando.kubernetes.model.common.EntandoBaseCustomResource;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public final class NotFoundExceptionFactory {

    private NotFoundExceptionFactory() {
    }

    public static ThrowableProblem generic(String message) {
        return Problem.valueOf(Status.NOT_FOUND, message);
    }

    public static ThrowableProblem entandoApp(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("EntandoApp with name %s not found in observed namespaces", name));
    }

    public static ThrowableProblem entandoPlugin(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("EntandoPlugin with name %s not found in observed namespaces", name));
    }

    public static ThrowableProblem configMap(String name, String namespace) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("ConfigMap with name %s not found in namespace %s", name, namespace));
    }

    public static ThrowableProblem entandoDeBundle(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("Bundle with name %s not found in observed namespace", name));
    }

    public static ThrowableProblem entandoLink(String appName, String pluginName) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("Link between EntandoApp %s and EntandoPlugin %s "
                        + "not found in observed namespace", appName, pluginName));
    }

    public static ThrowableProblem entandoLinkWithName(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("EntandoAppPluginLink with name %s "
                        + "not found in observed namespace", name));
    }

    public static ThrowableProblem observedNamespace(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("Namespace %s is not part of the observed namespaces", name));
    }

    public static ThrowableProblem ingress(EntandoBaseCustomResource r) {
        return Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .withDetail("Ingress not found for " + r.getKind() + " " + r.getMetadata().getName()
                        + " in namespace " + r.getMetadata().getNamespace())
                .build();

    }

    public static ThrowableProblem secret(String name, String key) {
        return Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .withDetail(String.format("Secret not found for name:'%s' and key: '%s'", name, key))
                .build();

    }

}
