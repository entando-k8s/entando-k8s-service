package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.entando.kubernetes.model.DbmsImageVendor;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec.EntandoPluginSpecBuilder;


@JsonDeserialize(builder = EntandoPluginSpecBuilder.class)
@SuppressWarnings({"PMD.UseConcurrentHashMap", "PMD.AccessorClassGeneration"})
public class EntandoPluginSpec implements KubernetesResource {

    @JsonProperty
    private final String entandoAppName;
    @JsonProperty
    private final String entandoAppNamespace;
    @JsonProperty
    private final String image;
    @JsonProperty
    private final int replicas;
    @JsonProperty
    private final String dbms;
    @JsonProperty
    private final List<ExpectedRole> roles;
    @JsonProperty
    private final List<Permission> permissions;
    @JsonProperty
    private final Map<String, Object> parameters = new HashMap<>();
    @JsonProperty
    private final String ingressPath;
    @JsonProperty
    private final String keycloakServerNamespace;
    @JsonProperty
    private final String keycloakServerName;
    @JsonProperty
    private final String healthCheckPath;

    private EntandoPluginSpec(EntandoPluginSpecBuilder builder) {
        this.entandoAppNamespace = builder.entandoAppNamespace;
        this.entandoAppName = builder.entandoAppName;
        this.image = builder.image;
        this.dbms = builder.dbms.toValue();
        this.replicas = builder.replicas;
        this.ingressPath = builder.ingressPath;
        this.keycloakServerNamespace = builder.keycloakServerNamespace;
        this.keycloakServerName = builder.keycloakServerName;
        this.healthCheckPath = builder.healthCheckPath;
        this.roles = builder.roles;
        this.permissions = builder.permissions;
    }

    @JsonIgnore
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @JsonIgnore
    public String getEntandoAppNamespace() {
        return entandoAppNamespace;
    }

    @JsonIgnore
    public String getImage() {
        return image;
    }

    @JsonIgnore
    public int getReplicas() {
        return replicas;
    }

    @JsonIgnore
    public String getEntandoAppName() {
        return entandoAppName;
    }

    @JsonIgnore
    public Optional<DbmsImageVendor> getDbms() {
        return Optional.ofNullable(DbmsImageVendor.forValue(dbms));
    }

    @JsonIgnore
    public List<ExpectedRole> getRoles() {
        return roles;
    }

    @JsonIgnore
    public List<Permission> getPermissions() {
        return permissions;
    }

    @JsonIgnore
    public String getIngressPath() {
        return ingressPath;
    }

    @JsonIgnore
    public String getHealthCheckPath() {
        return healthCheckPath;
    }

    @JsonIgnore
    public String getKeycloakServerNamespace() {
        return keycloakServerNamespace;
    }

    @JsonIgnore
    public String getKeycloakServerName() {
        return keycloakServerName;
    }

    @JsonPOJOBuilder
    @SuppressWarnings("PMD.TooManyMethods")
    public static class EntandoPluginSpecBuilder {

        private String entandoAppName;
        private String entandoAppNamespace;
        private String image;
        private int replicas = 1;
        private DbmsImageVendor dbms;
        private List<ExpectedRole> roles = new ArrayList<>();
        private List<Permission> permissions = new ArrayList<>();
        private Map<String, Object> parameters = new HashMap<>();
        private String ingressPath;
        private String keycloakServerNamespace;
        private String keycloakServerName;
        private String healthCheckPath;

        public EntandoPluginSpecBuilder withDbms(DbmsImageVendor dbms) {
            this.dbms = dbms;
            return this;
        }

        public EntandoPluginSpecBuilder withIngressPath(String ingressPath) {
            this.ingressPath = ingressPath;
            return this;
        }

        public EntandoPluginSpecBuilder withImage(String image) {
            this.image = image;
            return this;
        }

        public EntandoPluginSpecBuilder withKeycloakServer(String namespace, String name) {
            this.keycloakServerName = name;
            this.keycloakServerNamespace = namespace;
            return this;
        }

        public EntandoPluginSpecBuilder withEntandoAppName(String name) {
            this.entandoAppName = name;
            return this;
        }

        public EntandoPluginSpecBuilder withEntandoAppNamespace(String namespace) {
            this.entandoAppNamespace = namespace;
            return this;
        }

        public EntandoPluginSpecBuilder withEntandoApp(String namespace, String name) {
            this.entandoAppName = name;
            this.entandoAppNamespace = namespace;
            return this;
        }

        public EntandoPluginSpecBuilder withReplicas(Integer replicas) {
            this.replicas = replicas;
            return this;
        }

        public EntandoPluginSpecBuilder withRoles(List<ExpectedRole> roles) {
            this.roles = roles;
            return this;
        }

        public EntandoPluginSpecBuilder withPermissions(List<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public EntandoPluginSpecBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public EntandoPluginSpecBuilder withKeycloakServerNamespace(String keycloakServerNamespace) {
            this.keycloakServerNamespace = keycloakServerNamespace;
            return this;
        }

        public EntandoPluginSpecBuilder withKeycloakServerName(String keycloakServerName) {
            this.keycloakServerName = keycloakServerName;
            return this;
        }

        public EntandoPluginSpecBuilder addRole(String code, String name) {
            roles.add(new ExpectedRole(code, name));
            return this;
        }

        public EntandoPluginSpecBuilder addPermission(String clientId, String role) {
            permissions.add(new Permission(clientId, role));
            return this;
        }

        public EntandoPluginSpecBuilder addParameter(String name, Object value) {
            this.parameters.put(name, value);
            return this;
        }

        public EntandoPluginSpecBuilder withHealthCheckPath(String healthCheckPath) {
            this.healthCheckPath = healthCheckPath;
            return this;
        }

        public EntandoPluginSpec build() {
            return new EntandoPluginSpec(this);
        }

    }
}
