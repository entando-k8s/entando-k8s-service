package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import java.io.IOException;
import java.util.List;
import org.entando.kubernetes.model.DbmsVendor;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginBuilder;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.model.plugin.PluginSecurityLevel;
import org.springframework.core.io.ClassPathResource;


public class EntandoPluginTestHelper {

    public static final String BASE_PLUGIN_ENDPOINT = "/plugins";
    public static final String TEST_PLUGIN_NAME = "my-plugin";
    public static final String TEST_PLUGIN_NAMESPACE = "test-namespace";

    public static EntandoPlugin createTestEntandoPlugin(KubernetesClient client) {
        EntandoPlugin ep = getTestEntandoPlugin();

        KubernetesDeserializer
                .registerCustomKind(ep.getApiVersion(), ep.getKind(), EntandoPlugin.class);

        return getEntandoPluginOperations(client).inNamespace(ep.getMetadata().getNamespace()).createOrReplace(ep);

    }

    public static MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin,
            Resource<EntandoPlugin, DoneableEntandoPlugin>> getEntandoPluginOperations(KubernetesClient client) {
        CustomResourceDefinition entandoPluginCrd = createEntandoPluginCrd(client);

        return client.customResources(entandoPluginCrd, EntandoPlugin.class, EntandoPluginList.class,
                DoneableEntandoPlugin.class);
    }

    public static CustomResourceDefinition createEntandoPluginCrd(KubernetesClient client) {
        String entandoPluginCrdResource = "crd/EntandoPluginCRD.yaml";
        CustomResourceDefinition entandoPluginCrd = client.customResourceDefinitions().withName(EntandoPlugin.CRD_NAME)
                .get();
        if (entandoPluginCrd == null) {
            List<HasMetadata> list = null;
            try {
                list = client.load(new ClassPathResource(entandoPluginCrdResource).getInputStream())
                        .get();
            } catch (IOException e) {
                throw new RuntimeException("An error occurred while reading resource " + entandoPluginCrdResource, e);
            }
            entandoPluginCrd = (CustomResourceDefinition) list.get(0);
            // see issue https://github.com/fabric8io/kubernetes-client/issues/1486
            entandoPluginCrd.getSpec().getValidation().getOpenAPIV3Schema().setDependencies(null);
            return client.customResourceDefinitions().createOrReplace(entandoPluginCrd);
        }
        return entandoPluginCrd;
    }

    public static EntandoPlugin getTestEntandoPlugin() {
        EntandoPlugin entandoPlugin = new EntandoPluginBuilder().withNewSpec()
                .withImage("entando/entando-avatar-plugin")
                .withDbms(DbmsVendor.POSTGRESQL)
                .withReplicas(1)
                .withHealthCheckPath("/management/health")
                .withIngressPath("/dummyPlugin")
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

}
