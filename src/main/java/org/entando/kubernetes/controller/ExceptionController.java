package org.entando.kubernetes.controller;

import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionController implements ProblemHandling {

    @ExceptionHandler({NotObservedNamespaceException.class})
    public final void rethrowProblem(NotObservedNamespaceException ex) {
        throw BadRequestExceptionFactory.notObservedNamespace(ex.getNamespace());
    }

}
