package org.entando.kubernetes.config;

import org.entando.kubernetes.security.oauth2.JwtAuthorityExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = false)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    private static final String ADMIN = "ROLE_ADMIN";//From JHipster generated code.

    private final JwtAuthorityExtractor jwtAuthorityExtractor;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(JwtAuthorityExtractor jwtAuthorityExtractor, SecurityProblemSupport problemSupport) {
        this.problemSupport = problemSupport;
        this.jwtAuthorityExtractor = jwtAuthorityExtractor;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf()
                .disable()
                .exceptionHandling()
                .accessDeniedHandler(problemSupport)
                .and()
                .headers()
                .contentSecurityPolicy(
                        "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage"
                                + ".googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
                .and()
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                .featurePolicy(
                        "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; "
                                + "gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
                .and()
                .frameOptions()
                .deny()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers("/actuator/**").hasAuthority(ADMIN)
                .requestMatchers("/**").authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthorityExtractor);
        // @formatter:on
        return http.build();
    }

}
