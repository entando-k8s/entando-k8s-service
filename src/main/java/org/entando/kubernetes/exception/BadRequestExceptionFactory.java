package org.entando.kubernetes.exception;

import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public final class BadRequestExceptionFactory {

    private BadRequestExceptionFactory() {
    }

    public static ThrowableProblem pluginAlreadyDeployed(EntandoPlugin plugin) {
        return Problem.valueOf(Status.BAD_REQUEST,
                String.format("Plugin with name %s is already deployed in namespace %s",
                        plugin.getMetadata().getName(),
                        plugin.getMetadata().getNamespace()));
    }


}
