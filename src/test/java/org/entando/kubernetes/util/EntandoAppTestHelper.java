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
import org.entando.kubernetes.model.JeeServer;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.app.EntandoAppList;
import org.springframework.core.io.ClassPathResource;

public class EntandoAppTestHelper {

    public static final String BASE_APP_ENDPOINT = "/apps";
    public static final String TEST_APP_NAME = "my-app";
    public static final String TEST_APP_NAMESPACE = "test-namespace";

    public static EntandoApp createTestEntandoApp(KubernetesClient client) {
        EntandoApp ea = getTestEntandoApp();

        KubernetesDeserializer.registerCustomKind(ea.getApiVersion(), ea.getKind(), EntandoApp.class);

        return getEntandoAppOperations(client).inNamespace(ea.getMetadata().getNamespace()).createOrReplace(ea);
    }

    public static MixedOperation<EntandoApp, EntandoAppList, DoneableEntandoApp,
            Resource<EntandoApp, DoneableEntandoApp>> getEntandoAppOperations(KubernetesClient client) {
        CustomResourceDefinition entandoAppCrd = createEntandoAppCrd(client);

        return client.customResources(entandoAppCrd, EntandoApp.class, EntandoAppList.class,
                DoneableEntandoApp.class);
    }

    public static CustomResourceDefinition createEntandoAppCrd(KubernetesClient client) {
        String entandoAppCrdResource = "crd/EntandoAppCRD.yaml";
        CustomResourceDefinition entandoAppCrd = client.customResourceDefinitions().withName(EntandoApp.CRD_NAME)
                .get();
        if (entandoAppCrd == null) {
            List<HasMetadata> list = null;
            try {
                list = client.load(new ClassPathResource(entandoAppCrdResource).getInputStream())
                        .get();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("An error occurred while reading resource " + entandoAppCrdResource, e);
            }
            entandoAppCrd = (CustomResourceDefinition) list.get(0);
            // see issue https://github.com/fabric8io/kubernetes-client/issues/1486
            entandoAppCrd.getSpec().getValidation().getOpenAPIV3Schema().setDependencies(null);
            return client.customResourceDefinitions().createOrReplace(entandoAppCrd);
        }
        return entandoAppCrd;
    }

    public static EntandoApp getTestEntandoApp() {
        EntandoApp entandoApp = new EntandoAppBuilder().withNewSpec()
                .withDbms(DbmsVendor.POSTGRESQL)
                .withReplicas(1)
                .withStandardServerImage(JeeServer.WILDFLY)
                .endSpec()
                .build();

        entandoApp.setMetadata(new ObjectMetaBuilder()
                .withName(TEST_APP_NAME)
                .withNamespace(TEST_APP_NAMESPACE)
                .build());
        entandoApp.setApiVersion("entando.org/v1alpha1");
        return entandoApp;
    }

}
