package org.entando.kubernetes.controller;

import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotObservedNamespaceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@ControllerAdvice
public class ExceptionController implements ProblemHandling {

    @ExceptionHandler({NotObservedNamespaceException.class})
    public ResponseEntity<Problem> rethrowProblem(NotObservedNamespaceException ex, NativeWebRequest nwr) {
        return handleProblem(BadRequestExceptionFactory.notObservedNamespace(ex.getNamespace()), nwr);
    }

}
