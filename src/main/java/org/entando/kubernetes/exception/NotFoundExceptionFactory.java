package org.entando.kubernetes.exception;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public final class NotFoundExceptionFactory {

    private NotFoundExceptionFactory() {
    }

    public static ThrowableProblem entandoApp(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("EntandoApp with name %s not found in observed namespaces", name));
    }

    public static ThrowableProblem entandoPlugin(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("EntandoPlugin with name %s not found in observed namespaces", name));
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
    public static ThrowableProblem observedNamespace(String name) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("Namespace %s is not part of the observed namespaces", name));
    }

}
