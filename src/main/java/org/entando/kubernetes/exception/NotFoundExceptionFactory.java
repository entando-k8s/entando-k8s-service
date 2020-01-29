package org.entando.kubernetes.exception;

import org.entando.web.exception.HttpException;
import org.entando.web.exception.NotFoundException;
import org.springframework.http.HttpStatus;

public final class NotFoundExceptionFactory {

    private NotFoundExceptionFactory() {
    }

    public static NotFoundException entandoApp() {
        return new NotFoundException("org.entando.error.appNotFound");
    }

    public static NotFoundException entandoPlugin() {
        return new NotFoundException("org.entando.error.pluginNotFound");
    }

    public static HttpException entandoDeBundle(String name, String namespace) {
        return new HttpException(HttpStatus.NOT_FOUND,
                "org.entando.error.bundleNotFound",
                new String[]{name, namespace});
    }

}
