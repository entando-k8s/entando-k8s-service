package org.entando.kubernetes.exception;

import org.entando.web.exception.NotFoundException;

public class EntandoAppNotFoundException extends NotFoundException {

    public EntandoAppNotFoundException() {
        super("org.entando.error.appNotFound");
    }

}
