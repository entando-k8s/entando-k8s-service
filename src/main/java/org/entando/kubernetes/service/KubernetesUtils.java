package org.entando.kubernetes.service;

import static java.util.Optional.ofNullable;

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
    @SuppressWarnings("java:S5164")
    private final ThreadLocal<String> callerNamespace = new ThreadLocal<>();

    public KubernetesUtils() {
        this(s -> new DefaultKubernetesClient());
    }

    public KubernetesUtils(Function<String, KubernetesClient> defaultKubernetesClientSupplier) {
        this.currentToken.set(DefaultKubernetesClientBuilder.NOT_K8S_TOKEN);
        this.kubernetesClients = new KubernetesClientCache(defaultKubernetesClientSupplier);
    }

    public String getDefaultPluginNamespace() {
        return ofNullable(callerNamespace.get()).orElse(getCurrentNamespace());
    }

    public String getCurrentNamespace() {
        return getCurrentKubernetesClient().getNamespace();
    }

    public KubernetesClient getCurrentKubernetesClient() {
        //at this point, we always use the service account of the entando-k8s-service which should be the same as the operator's service
        // account
        return this.kubernetesClients.get(DefaultKubernetesClientBuilder.NOT_K8S_TOKEN);
        //If we ever require serviceAccount propagation from component-manager, reactivate this line:
        //return this.kubernetesClients.get(currentToken.get());
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            final JWT parsedJwt = JWTParser.parse(token);
            if (parsedJwt.getJWTClaimsSet().getClaims().get("kubernetes.io/serviceaccount/namespace") != null) {
                //Leaving this here for now. We may still want to use the consumer's K8S token, but for now
                // none of our consumers are passing the K8S token
                this.currentToken.set(token);
                this.callerNamespace.set((String) parsedJwt.getJWTClaimsSet().getClaims().get("kubernetes.io/serviceaccount/namespace"));

                //Some possible fields to use:
                //iss
                //kubernetes.io/serviceaccount/service-account.name
                //kubernetes.io/serviceaccount/service-account.uid
            } else {
                this.callerNamespace.remove();
                this.currentToken.set(DefaultKubernetesClientBuilder.NOT_K8S_TOKEN);

            }
            Map<String, Object> claims = new LinkedHashMap<>(parsedJwt.getJWTClaimsSet().getClaims());
            claims.put(ROLES,
                    //TODO For now, everyone is an admin. In future we may want to limit the creation of plugins
                    // across namespaces
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            if (claims.get(JwtClaimNames.IAT) instanceof Date) {
                claims.put(JwtClaimNames.IAT, ((Date) claims.get(JwtClaimNames.IAT)).toInstant());
            }
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
