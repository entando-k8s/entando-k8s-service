package org.entando.kubernetes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.fabric8.zjsonpatch.internal.guava.Strings;

public enum JeeServer {
    WILDFLY("entando/entando-de-app-wildfly"),
    EAP("entando/entando-de-app-eap"),
    TOMCAT("entando/entando-de-app-tomcat"),
    JETTY("entando/entando-de-app-jetty");

    private final String imageName;

    JeeServer(String imageName) {
        this.imageName = imageName;
    }

    @JsonCreator
    public static JeeServer forValue(String value) {
        return Strings.isNullOrEmpty(value) ? null : JeeServer.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    public String getImageName() {
        return imageName;
    }
}
