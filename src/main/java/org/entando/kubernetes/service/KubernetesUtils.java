package org.entando.kubernetes.service;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
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
                throw new JwtException("Client credentials flow token detected but not supported anymore. Please use K8S tokens");
            }
            Map<String, Object> claims = new LinkedHashMap<>(parsedJwt.getJWTClaimsSet().getClaims());
            claims.put(ROLES,
                    //For now, everyone is an admin
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            // TODO the K8S token hasn't exp anymore => delete check this too?
            if (claims.get(JwtClaimNames.EXP) instanceof Date) {
                claims.put(JwtClaimNames.EXP, ((Date) claims.get(JwtClaimNames.EXP)).toInstant());
            }
            Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> {
                        c.putAll(claims);
                    }).build();
        } catch (ParseException e) {
            throw new JwtException("Malformed payload", e);
        } catch (Exception e) {
            //TODO in future this can be taken out.
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not process JWT token.", e);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Token:" + token);
            throw new IllegalArgumentException(e);
        }
    }
}
