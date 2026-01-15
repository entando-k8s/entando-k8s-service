package org.entando.kubernetes.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

/**
 * Controller advice to handle exceptions and convert them to Problem (RFC 7807) responses.
 */
@ControllerAdvice
public class ExceptionHandlerAdvice implements ProblemHandling {
}