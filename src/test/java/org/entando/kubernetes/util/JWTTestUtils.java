package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.entando.kubernetes.model.DbmsVendor;
import org.entando.kubernetes.model.JeeServer;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.app.EntandoAppOperationFactory;
import org.entando.kubernetes.service.DefaultKubernetesClientBuilder;
import org.entando.kubernetes.service.KubernetesUtils;

public class JWTTestUtils {

    // namespace fireone, like the one returned by the mock kubernetes client supplied by fabric8
    public static final String JWT = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjZsQU9XaTMwUFE2TVlWMFRKVUh5eExCT1pvS0ZxOTBDTG5nRk9lbX"
            + "QwR00ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3B"
            + "hY2UiOiJmaXJlb25lIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImRlZmF1bHQtdG9rZW4tend4Y3M"
            + "iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZGVmYXVsdCIsImt1YmVybmV0ZXMuaW8"
            + "vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjIwOTQ3NzBkLTQ2MmUtNDJhZi1iNzcyLWVmMmY2YmZmOTk2ZCIsInN"
            + "1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpmaXJlb25lOmRlZmF1bHQifQ.FHy3iGfuBLGaVfqrsSjBgy3mxIKIV824IpK74MLWsDkM"
            + "--hRevD0Qm6hGgOx2pbvhAdbDxculpYg4dWwi6zfoPEMPSg8gG9tQfqoYOoN2jMkaUOftCe8DnQzaearANz80E5g5CBvrtpK9P-ujNS7"
            + "A5lUavzrSfCmKVfiUkHKaGhJOdnpdMvLtKW4N1PWmSaJ-ArfBqjn_cKMmssZ_P4FFNqmM8uxLXGuKb6dq7D5x6HqsSGPnJZuVTFkpHAL0"
            + "YEVGLxPDhklmqxvj-OwwzLCnx4NKoh9ngYwYHVNMwOH8i5U2VCZMzqIJg-B6jUHFjxjU8k9NLFS38GI92Ljx1Ec3g";

    /**
     * uses the received KubernetesUtils to decode the fake JWT and to set the data obtained by the read claims.
     * @param k8sUtils the KubernetesUtils to use to decode the fake JWT
     */
    public static void decodeFakeJWT(KubernetesUtils k8sUtils) {
        k8sUtils.decode(JWT);
    }
}
