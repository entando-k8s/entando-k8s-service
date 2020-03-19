package org.entando.kubernetes.exception;

import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.request.AppPluginLinkRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public final class BadRequestExceptionFactory {

    private BadRequestExceptionFactory() {
    }

    public static ThrowableProblem pluginAlreadyDeployed(EntandoPlugin plugin) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Plugin with name %s is already deployed in observed namespaces",
                        plugin.getMetadata().getName()));
    }

    public static ThrowableProblem pluginNamespaceNotObserved(EntandoPlugin plugin) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Provided plugin %s namespace %s is not observed by the service and therefore not usable",
                        plugin.getMetadata().getName(),
                        plugin.getMetadata().getNamespace()
                ));
    }

    public static ThrowableProblem invalidNamespace(String namespace) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Provided namespace %s isn't valid for regex '[a-z0-9]([-a-z0-9]*[a-z0-9])?'", namespace));
    }

    public static ThrowableProblem notObservedNamespace(String namespace) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Provided namespace %s is not part of namespaces observed by this instance", namespace));
    }

    public static ThrowableProblem notObservedNamespace(NotObservedNamespaceException ex) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Provided namespace %s is not part of namespaces observed by this instance", ex.getNamespace()));
    }

    public static ThrowableProblem invalidAppPluginLinkDeleteRequest(AppPluginLinkRequest req) {
        String message = "Invalid link delete request! appName and pluginName must be not null nor empty - " + req.toString();
        return Problem.valueOf(Status.BAD_REQUEST, message);
    }
}
