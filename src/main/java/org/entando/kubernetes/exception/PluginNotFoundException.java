package org.entando.kubernetes.exception;

import org.entando.web.exception.NotFoundException;
import org.springframework.http.HttpStatus;

public class PluginNotFoundException extends NotFoundException {

    public PluginNotFoundException() {
        super("org.entando.error.pluginNotFound");
    }

}
