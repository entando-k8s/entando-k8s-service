package org.entando.kubernetes.security.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.entando.kubernetes.config.KubernetesConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@Tag("unit")
@SuppressWarnings("java:S5786")
//because we are using this class from other tests.
public class KubernetesUtilsTest {

    public static final String NON_K8S_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.dyt0CoTl4WoVjAHI9Q_CwSKhl6d_9rhM3NrXuJttkao";

    public static final String K8S_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6InloSmE0VVFLcDhkRkx4cXduT284ME5OUnFOX1VYdnZncFdaMDdDb2VfdmcifQ"
            +
            ".eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJxZS10d28iLCJrdWJ"
            + "lcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoidGVzdC1hY2NvdW50LXRva2VuLTJubnY3Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlY"
            + "WNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6InRlc3QtYWNjb3VudCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW5"
            + "0LnVpZCI6ImQ5MzJjYmFhLWI0NGQtNDE5YS04ZGExLTA3ZmY2MDQ5NjQ5OCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpxZS10d286dGVzdC1hY"
            + "2NvdW50In0.Ds0JFtX7VcAteczE9YHxIxsqYfN210UcqR_RnXs-1aTlDtAsZ5SJlrW9GuYUb6pDkR6f78NviUYCx6V4oUZbZUCYMfEo7t02h-x3k0GB2Ed"
            + "4Sa1cbhh9ilyKpA4H2AYHx-p-hqeb4CQnJrhCrV6cm3vxgNsQZifAAcSkbVo8DyiVY14cf4JhfaXwRCzSyO8Og-rXNVnLQw8ZvIH3OrIIaX7DGKIR-6eWW"
            + "O8rgpzCW-f61jbZG-GcFa6nfT68FknqgRfSIFkBgpl7rZZ6iO0vyr3SR-atZnbf3azbpM2OMTHmoCQWUihxNduVvfTkLEviEiDpSLXsMzbuuLOpAdaYgg";

    @Test
    void shouldHavePredefinedRoles() {
        final JwtDecoder jwtDecoder = new KubernetesConfiguration().k8sUtils();
        final Jwt decode = jwtDecoder.decode(K8S_TOKEN);
        List<GrantedAuthority> grantedAuthorities = decode.getClaim("roles");
        assertThat(grantedAuthorities.get(0).getAuthority()).isEqualTo("ROLE_ADMIN");
    }

}
