package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.entando.kubernetes.model.DbmsVendor;
import org.entando.kubernetes.model.JeeServer;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppBuilder;
import org.entando.kubernetes.model.app.EntandoAppList;
import org.entando.kubernetes.model.app.EntandoAppOperationFactory;

public class EntandoAppTestHelper {

    public static final String BASE_APP_ENDPOINT = "/apps";
    public static final String TEST_APP_NAME = "my-app";
    public static final String TEST_APP_NAMESPACE = "test-namespace";

    public static EntandoApp createTestEntandoApp(KubernetesClient client) {
        EntandoApp ea = getTestEntandoApp();

        KubernetesDeserializer.registerCustomKind(ea.getApiVersion(), ea.getKind(), EntandoApp.class);

        return EntandoAppOperationFactory
                .produceAllEntandoApps(client)
                .inNamespace(ea.getMetadata().getNamespace()).createOrReplace(ea);
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
