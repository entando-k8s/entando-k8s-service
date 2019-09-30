package org.entando.kubernetes.exception;

import org.entando.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public class UnsetEnvVarsException extends HttpException {

    public UnsetEnvVarsException(final Object... envs) {
        super(HttpStatus.BAD_REQUEST, "org.entando.error.unsetVarsException", envs);
    }

}
