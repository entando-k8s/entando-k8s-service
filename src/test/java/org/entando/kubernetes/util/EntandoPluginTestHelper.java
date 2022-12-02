package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.entando.kubernetes.model.common.DbmsVendor;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.model.plugin.PluginSecurityLevel;

public class EntandoPluginTestHelper {

    public static final String BASE_PLUGIN_ENDPOINT = "/plugins";
    public static final String TEST_PLUGIN_NAME = "my-plugin";
    public static final String TEST_PLUGIN_NAMESPACE = "test-namespace";

    public static EntandoPlugin createTestEntandoPlugin(KubernetesClient client) {
        EntandoPlugin ep = getTestEntandoPlugin();
        KubernetesDeserializer.registerCustomKind(ep.getApiVersion(), ep.getKind(), EntandoPlugin.class);
        return getEntandoPluginOperations(client).inNamespace(ep.getMetadata().getNamespace()).createOrReplace(ep);
    }

    public static EntandoPlugin getTestEntandoPlugin() {
        EntandoPlugin entandoPlugin = new EntandoPluginBuilder().withNewSpec()
                .withImage("entando/entando-avatar-plugin")
                .withDbms(DbmsVendor.POSTGRESQL)
                .withReplicas(1)
                .withHealthCheckPath("/management/health")
                .withIngressPath("/dummyPlugin")
                .withCustomIngressPath("/dummyCustomPath")
                .withSecurityLevel(PluginSecurityLevel.LENIENT)
                .withIngressHostName("dummyPlugin.test")
                .endSpec()
                .build();

        entandoPlugin.setMetadata(new ObjectMetaBuilder()
                .withName(TEST_PLUGIN_NAME)
                .withNamespace(TEST_PLUGIN_NAMESPACE)
                .build());
        entandoPlugin.setApiVersion("entando.org/v1alpha1");
        return entandoPlugin;
    }

    public static EntandoPlugin getTestEntandoPluginNoIngressPath() {
        EntandoPlugin entandoPlugin = new EntandoPluginBuilder().withNewSpec()
                .withImage("entando/entando-avatar-plugin")
                .withDbms(DbmsVendor.POSTGRESQL)
                .withReplicas(1)
                .withHealthCheckPath("/management/health")
                .withSecurityLevel(PluginSecurityLevel.LENIENT)
                .withIngressHostName("dummyPlugin.test")
                .endSpec()
                .build();

        entandoPlugin.setMetadata(new ObjectMetaBuilder()
                .withName(TEST_PLUGIN_NAME)
                .withNamespace(TEST_PLUGIN_NAMESPACE)
                .build());
        entandoPlugin.setApiVersion("entando.org/v1alpha1");
        return entandoPlugin;
    }

    public static MixedOperation<EntandoPlugin, KubernetesResourceList<EntandoPlugin>, Resource<EntandoPlugin>> getEntandoPluginOperations(
            KubernetesClient client) {
        return client.customResources(EntandoPlugin.class);
    }
}
