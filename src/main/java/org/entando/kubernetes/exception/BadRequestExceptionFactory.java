package org.entando.kubernetes.exception;

import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public final class BadRequestExceptionFactory {

    private BadRequestExceptionFactory() {
    }

    public static HttpClientErrorException pluginAlreadyDeployed(EntandoPlugin plugin) {
        return new HttpClientErrorException(HttpStatus.BAD_REQUEST,
                String.format("Plugin with name %s is already deployed in namespace %s",
                plugin.getMetadata().getName(), plugin.getMetadata().getNamespace()));
    }


}
