package org.entando.kubernetes.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@Profile("dev")
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("${entando.cors.allowed-origins-dev:}")
    private List<String> entandoCorsAllowedOrigins = new ArrayList<>();

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = entandoCorsAllowedOrigins.toArray(new String[entandoCorsAllowedOrigins.size()]);
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("*")
                .allowCredentials(true);

    }
}
