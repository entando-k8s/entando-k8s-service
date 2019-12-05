package org.entando.kubernetes.exception;

import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public final class BadRequestExceptionFactory {

    private BadRequestExceptionFactory() {
    }

    public static HttpException pluginAlreadyDeployed(EntandoPlugin plugin) {
        return new HttpException(HttpStatus.BAD_REQUEST,
                "org.entando.error.pluginAlreadyDeployed",
                new String[]{plugin.getMetadata().getName(), plugin.getMetadata().getNamespace()});
    }


}
