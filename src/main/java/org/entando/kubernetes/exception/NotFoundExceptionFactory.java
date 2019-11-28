package org.entando.kubernetes.exception;

import org.entando.web.exception.NotFoundException;

public final class NotFoundExceptionFactory {
    private NotFoundExceptionFactory() {}

    public static NotFoundException deployment() {
        return new NotFoundException("org.entando.error.deploymentNotFound");
    }

    public static NotFoundException entandoApp() {
        return new NotFoundException("org.entando.error.appNotFound");
    }

    public static NotFoundException entandoPlugin() {
        return new NotFoundException("org.entando.error.pluginNotFound");
    }

}
