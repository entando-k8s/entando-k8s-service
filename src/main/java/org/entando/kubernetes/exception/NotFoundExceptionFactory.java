package org.entando.kubernetes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

public final class NotFoundExceptionFactory {

    private NotFoundExceptionFactory() {
    }

    public static HttpClientErrorException entandoApp() {
        return new HttpClientErrorException(HttpStatus.NOT_FOUND, "org.entando.error.appNotFound");
    }

    public static HttpClientErrorException entandoPlugin() {
        return new HttpClientErrorException(HttpStatus.NOT_FOUND, "org.entando.error.pluginNotFound");
    }

    public static HttpClientErrorException entandoDeBundle(String bundleName, String bundleNamespace) {
        return new HttpClientErrorException(HttpStatus.NOT_FOUND, "org.entando.error.pluginAlreadyDeployed");
//                new String[]{bundleName, bundleNamespace});
    }

}
