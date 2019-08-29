package org.entando.kubernetes.exception;

import org.entando.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public class PluginAlreadyDeployedException extends HttpException {

    public PluginAlreadyDeployedException() {
        super(HttpStatus.BAD_REQUEST, "org.entando.error.pluginAlreadyDeployed");
    }

}
