package org.entando.kubernetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@ComponentScan("org.entando")
@SuppressWarnings("PMD")
public class EntandoKubernetesJavaApplication {

    public static void main(final String[] args) {
        SpringApplication.run(EntandoKubernetesJavaApplication.class, args);
    }

}
