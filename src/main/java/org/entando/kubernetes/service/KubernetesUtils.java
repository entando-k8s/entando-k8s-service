package org.entando.kubernetes.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.text.ParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

public class KubernetesUtils implements JwtDecoder {

    public static final String ROLES = "roles";
    private final KubernetesClientCache kubernetesClients;
    @SuppressWarnings("java:S5164")
    //Because a single string per active thread cannot cause a memory leak.
    //This is exactly why we do not cache the KubernetesClient instance here.
    private final ThreadLocal<String> currentToken = new ThreadLocal<>();

    public KubernetesUtils() {
        this(s -> new DefaultKubernetesClient());
    }

    public KubernetesUtils(Function<String, KubernetesClient> defaultKubernetesClientSupplier) {
        this.currentToken.set(DefaultKubernetesClientBuilder.NOT_K8S_TOKEN);
        this.kubernetesClients = new KubernetesClientCache(defaultKubernetesClientSupplier);
    }

    public String getCurrentNamespace() {
        return getCurrentKubernetesClient().getNamespace();
    }

    public KubernetesClient getCurrentKubernetesClient() {
        return this.kubernetesClients.get(currentToken.get());
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            final JWT parsedJwt = JWTParser.parse(token);
            if (parsedJwt.getJWTClaimsSet().getClaims().get("kubernetes.io/serviceaccount/namespace") != null) {
                this.currentToken.set(token);
                //Some possible fields to use:
                //iss
                //kubernetes.io/serviceaccount/service-account.name
                //kubernetes.io/serviceaccount/service-account.uid
            } else {
                //TODO once component-manager has been migrated, throw an exception here. We only support K8S tokens
                this.currentToken.set(DefaultKubernetesClientBuilder.NOT_K8S_TOKEN);
            }
            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            final JWTClaimsSet jwtClaimsSet = parsedJwt.getJWTClaimsSet();
            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> {
                        c.put(ROLES,
                                //For now, everyone is an admin
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                        c.putAll(jwtClaimsSet.getClaims());
                    }).build();

        } catch (ParseException e) {
            throw new JwtException("Malformed payload", e);
        }
    }
}
