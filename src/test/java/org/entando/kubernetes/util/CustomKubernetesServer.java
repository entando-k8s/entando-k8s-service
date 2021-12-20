package org.entando.kubernetes.util;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.TlsVersion;

public class CustomKubernetesServer extends KubernetesServer {

    public static final TlsVersion[] TLS_VERSIONS =
            {TlsVersion.TLS_1_0, TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_3};
    private NamespacedKubernetesClient clientOverride;

    public CustomKubernetesServer(boolean https, boolean crudMode) {
        super(https, crudMode);
    }

    @Override
    synchronized public NamespacedKubernetesClient getClient() {
        if (clientOverride == null) {
            ConfigBuilder cb = new ConfigBuilder();
            Config config = cb.withMasterUrl(this.getMockServer().url("/").toString())
                    .withTrustCerts(true)
                    .withTlsVersions(TLS_VERSIONS)
                    .withNamespace("test")
                    .build();
            clientOverride = new DefaultKubernetesClient(HttpClientUtils.createHttpClientForMockServer(config),
                    config);
        }
        return clientOverride;
    }
}
