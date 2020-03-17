package org.entando.kubernetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
@ComponentScan("org.entando")
@EnableSwagger2
@SuppressWarnings("PMD")
public class EntandoKubernetesJavaApplication {

    public static void main(final String[] args) {
        SpringApplication.run(EntandoKubernetesJavaApplication.class, args);
    }

}
