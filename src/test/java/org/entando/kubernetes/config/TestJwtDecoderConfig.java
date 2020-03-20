package org.entando.kubernetes.config;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
@Profile("test")
public class TestJwtDecoderConfig {
    /*
    Had to split this because spring was not able to override the Bean already defined in SecurityConfiguration,
    while in the entando-component-manager this is not giving any issue at all
     */

    @Bean
    JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

}
