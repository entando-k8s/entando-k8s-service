package org.entando.kubernetes.exception;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

public final class NotFoundExceptionFactory {

    private NotFoundExceptionFactory() {
    }

    public static ThrowableProblem entandoApp() {
        return Problem.valueOf(Status.NOT_FOUND);
    }

    public static ThrowableProblem entandoPlugin() {
        return Problem.valueOf(Status.NOT_FOUND);
    }

    public static ThrowableProblem entandoDeBundle(String name, String namespace) {
        return Problem.valueOf(Status.NOT_FOUND,
                String.format("Bundle with name %s not found in namespace %s", name, namespace));
    }

}
