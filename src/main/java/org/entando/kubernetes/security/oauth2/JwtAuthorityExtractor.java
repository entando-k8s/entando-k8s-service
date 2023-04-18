package org.entando.kubernetes.security.oauth2;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.entando.kubernetes.service.KubernetesUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthorityExtractor extends JwtAuthenticationConverter {

    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return this.extractAuthorityFromClaims(jwt.getClaims());
    }

    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        //Everyone is an ADMIN
        return (List<GrantedAuthority>) claims.get(KubernetesUtils.ROLES);
    }
}
