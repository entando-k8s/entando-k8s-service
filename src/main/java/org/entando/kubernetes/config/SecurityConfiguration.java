package org.entando.kubernetes.config;

import org.entando.kubernetes.security.oauth2.JwtAuthorityExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    private static final String ADMIN = "ROLE_ADMIN";

    private final JwtAuthorityExtractor jwtAuthorityExtractor;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(JwtAuthorityExtractor jwtAuthorityExtractor,
                                 SecurityProblemSupport problemSupport) {
        this.problemSupport = problemSupport;
        this.jwtAuthorityExtractor = jwtAuthorityExtractor;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.accessDeniedHandler(problemSupport))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com;"
                                      + " style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .permissionsPolicyHeader(permissions -> permissions
                                .policy("geolocation=(none), midi=(none), sync-xhr=(none), microphone=(none), camera=(none),"
                                        + " magnetometer=(none), gyroscope=(none), speaker=(none), fullscreen=(self), payment=(none)")
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/**").hasAuthority(ADMIN)
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthorityExtractor))
                );

        return http.build();
    }
}
