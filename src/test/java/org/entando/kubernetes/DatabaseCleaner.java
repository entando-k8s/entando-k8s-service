package org.entando.kubernetes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseCleaner {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    public void cleanup() throws SQLException {
        DriverManager.getConnection(datasourceUrl, datasourceUsername, datasourcePassword)
                .createStatement()
                .execute("TRUNCATE TABLE digital_exchange CASCADE");
    }

}
