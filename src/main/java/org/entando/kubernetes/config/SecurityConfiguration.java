//package org.entando.kubernetes.config;
//
//import java.util.EnumSet;
//import java.util.List;
//import java.util.Set;
//import javax.servlet.DispatcherType;
//import org.entando.kubernetes.security.oauth2.JwtAuthorityExtractor;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.Ordered;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
//import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.springframework.web.filter.ForwardedHeaderFilter;
//import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
//
//@Profile("never")
////@EnableWebSecurity(debug = true)
////@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
////@Import(SecurityProblemSupport.class)
//public class SecurityConfiguration /*extends WebSecurityConfigurerAdapter*/ {
//
//    private static final String ADMIN = "ROLE_ADMIN";//From JHipster generated code.
//
//    private final JwtAuthorityExtractor jwtAuthorityExtractor;
//    private final SecurityProblemSupport problemSupport;
//
//    public SecurityConfiguration(JwtAuthorityExtractor jwtAuthorityExtractor, SecurityProblemSupport problemSupport) {
//        this.problemSupport = problemSupport;
//        this.jwtAuthorityExtractor = jwtAuthorityExtractor;
//    }
//
////    @Override
//    public void configure(HttpSecurity http) throws Exception {
//        // @formatter:off
////        http.addFilterBefore(new ForwardedHeaderFilter(), OAuth2AuthorizationRequestRedirectFilter.class)
//
//        http
//                .csrf()
//                .disable()
//                .exceptionHandling()
//                .accessDeniedHandler(problemSupport)
//                .and()
//                .headers()
//                .contentSecurityPolicy(
//                        "default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage"
//                                + ".googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
//                .and()
//                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
//                .and()
//                .featurePolicy(
//                        "geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; "
//                                + "gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
//                .and()
//                .frameOptions()
//                .deny()
//                .and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                .authorizeRequests()
//                .antMatchers("/actuator/health").permitAll()
//                .antMatchers("/actuator/info").permitAll()
//                .antMatchers("/actuator/prometheus").permitAll()
//                .antMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
//                .antMatchers("/actuator/**").permitAll()
//                .antMatchers("/bundles").permitAll()
//                .antMatchers("/bundles/**").permitAll()
////                .antMatchers("/actuator/**").hasAuthority(ADMIN)
//                .antMatchers("/**").authenticated()
//                .and()
//                .oauth2ResourceServer()
//                .jwt()
//                .jwtAuthenticationConverter(jwtAuthorityExtractor)
//                .and()
//                .and()
//                .oauth2Client();
//        // @formatter:on
//    }
//
//
////    @Bean
////    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
////        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>(new ForwardedHeaderFilter());
////        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
//////        registration.setDispatcherTypes(DispatcherType.FORWARD, DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
////        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
//////        registration.setUrlPatterns(List.of("/absoluteURLWithFilter"));
////        return registration;
////    }
//}
