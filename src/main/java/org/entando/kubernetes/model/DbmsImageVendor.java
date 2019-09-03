package org.entando.kubernetes.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.fabric8.zjsonpatch.internal.guava.Strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.format;

/**
 * Metadata related to Database vendors. Some of the metadata relates to the Docker images being used.
 * Entando creates one database for each EntandoApp deployment and one for each EntandoPlugin deployment.
 * For each datasource requirement inside the deployment, Entando will create a different schema/username/password combination.
 * For maximum portability, we enforce the pattern of using the schema name as the username
 * On deployment, Entando initializes the database image just enough to be able to create schemas against
 * MySQL considers databases to be schemas (!) so we only initialize the Root user's password
 * Oracle's images have been troublesome, and are not supported yet. For Oracle, rather use a pre-configured external database
 */
public enum DbmsImageVendor {
    MYSQL("docker.io/centos/mysql-57-centos7:latest", 3306, "root", "/var/lib/mysql/data", "MYSQL_PWD=${MYSQL_ROOT_PASSWORD} mysql -h 127.0.0.1 -u root -e 'SELECT 1'",  "org.hibernate.dialect.MySQL5InnoDBDialect") {
        public String buildJdbcConnectionString(String host, String port, String database, String schema) {
            return format("jdbc:mysql://%s:%s/%s", host, port, schema);
        }

        @Override
        public boolean schemaIsDatabase() {
            return true;
        }
    },
    POSTGRESQL("docker.io/centos/postgresql-96-centos7:latest", 5432, "postgres", "/var/lib/pgsql/data", "psql -h 127.0.0.1 -U ${POSTGRESQL_USER} -q -d postgres -c '\\l'|grep ${POSTGRESQL_DATABASE}", "org.hibernate.dialect.PostgreSQLDialect") {
        public String buildJdbcConnectionString(String host, String port, String database, String schema) {
            return format("jdbc:postgresql://%s:%s/%s", host, port, database);
        }

    },
    ORACLE("docker.io/store/oracle/database-enterprise:12.2.0.1", 1521, "sys", "/ORCL", "sqlplus sys/Oradoc_db1:${DB_SID}", "org.hibernate.dialect.Oracle10gDialect") {
        public Collection<ConfigVariable> getAdditionalConfig() {
            return Arrays.asList(
                    new ConfigVariable("oracleMavenRepo", "ORACLE_MAVEN_REPO"),
                    new ConfigVariable("oracleRepoUser", "ORACLE_REPO_USER"),
                    new ConfigVariable("oracleRepoPassword", "ORACLE_REPO_PASSWORD"),
                    new ConfigVariable("oracleTablespace", "TABLESPACE")
            );
        }

        public String buildJdbcConnectionString(String host, String port, String database, String schema) {
            return format("jdbc:oracle:thin:@//%s:%s/%s", host, port, database);
        }

    },
    NONE("", 0,"", "", "", "") {
        @Override
        public String buildJdbcConnectionString(String host, String port, String database, String schema) {
            return null;
        }
    };

    private String imageName;
    private int port;
    private String defaultAdminUsername;
    private String volumeMountPath;
    private String healthCheck;
    private String hibernateDialect;

    DbmsImageVendor(String imageName, int port, String defaultAdminUsername, String volumeMountPath, String healthCheck, String hibernateDialect) {
        this.imageName = imageName;
        this.port = port;
        this.defaultAdminUsername = defaultAdminUsername;
        this.volumeMountPath = volumeMountPath;
        this.healthCheck = healthCheck;
        this.hibernateDialect = hibernateDialect;
    }

    public abstract String buildJdbcConnectionString(String host, String port, String database, String schema);

    public String getHealthCheck() {
        return healthCheck;
    }

    public String getImageName() {
        return imageName;
    }

    public int getPort() {
        return port;
    }

    public String getVolumeMountPath() {
        return volumeMountPath;
    }

    @JsonCreator
    public static DbmsImageVendor forValue(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        return DbmsImageVendor.valueOf(value.toUpperCase());
    }


    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }


    public String getName() {
        return name().toLowerCase();
    }

    //TODO localize this elsewhere

    /**
     * Additional configuration parameters extracted from secrets provided for databases by customers
     *
     * @return
     */
    public Collection<ConfigVariable> getAdditionalConfig() {
        return Collections.emptyList();
    }

    public boolean schemaIsDatabase() {
        return false;
    }

    public String getDefaultAdminUsername() {
        return defaultAdminUsername;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public static class ConfigVariable {
        private String configKey;
        private String environmentVariable;

        public ConfigVariable(String configKey, String environmentVariable) {
            this.configKey = configKey;
            this.environmentVariable = environmentVariable;
        }

        public String getConfigKey() {
            return configKey;
        }

        public String getEnvironmentVariable() {
            return environmentVariable;
        }
    }

}
