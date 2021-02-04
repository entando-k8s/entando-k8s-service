package org.entando.kubernetes.util;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleBuilder;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.debundle.EntandoDeBundleOperationFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundleSpec;
import org.entando.kubernetes.model.debundle.EntandoDeBundleSpecBuilder;

public class EntandoDeBundleTestHelper {

    public static final String BASE_BUNDLES_ENDPOINT = "/bundles";
    public static final String TEST_BUNDLE_NAME = "my-bundle";
    public static final String TEST_BUNDLE_NAMESPACE = "test-namespace";

    public static EntandoDeBundle createTestEntandoDeBundle(KubernetesClient client) {
        EntandoDeBundle eb = getTestEntandoDeBundle();

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoDeBundle.class);

        return ((MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle,
                DoneableEntandoDeBundle>>) EntandoDeBundleOperationFactory
                .produceAllEntandoDeBundles(client))
                .inNamespace(eb.getMetadata().getNamespace()).createOrReplace(eb);

    }

    public static EntandoDeBundle createTestEntandoDeBundleInNamespace(KubernetesClient client, String namespace) {
        EntandoDeBundle eb = getTestEntandoDeBundle();
        eb.getMetadata().setNamespace(namespace);

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoDeBundle.class);

        return ((MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle, Resource<EntandoDeBundle,
                DoneableEntandoDeBundle>>) EntandoDeBundleOperationFactory
                .produceAllEntandoDeBundles(client))
                .inNamespace(namespace).createOrReplace(eb);

    }

    public static EntandoDeBundleSpec getTestEntandoDeBundleSpec() {
        return new EntandoDeBundleSpecBuilder()
                .withNewDetails()
                .withDescription("A bundle containing some demo components for Entando6")
                .withName("inail_bundle")
                .addNewVersion("0.0.1")
                .addNewKeyword("entando6")
                .addNewKeyword("digital-exchange")
                .addNewDistTag("latest", "0.0.1")
                .and()
                .addNewTag()
                .withVersion("0.0.1")
                .withIntegrity("sha512-n4TEroSqg/sZlEGg2xj6RKNtl/t3ZROYdNd99/dl3UrzCUHvBrBxZ1rxQg/sl3kmIYgn3+ogbIFmUZYKWxG3Ag==")
                .withShasum("4d80130d7d651176953b5ce470c3a6f297a70815")
                .withTarball("http://localhost:8081/repository/npm-internal/inail_bundle/-/inail_bundle-0.0.1.tgz")
                .endTag()
                .build();
    }

    public static EntandoDeBundle getTestEntandoDeBundle() {

        EntandoDeBundle bundle = new EntandoDeBundleBuilder()
                .withNewMetadata()
                .withName(TEST_BUNDLE_NAME)
                .withNamespace(TEST_BUNDLE_NAMESPACE)
                .endMetadata()
                .withSpec(getTestEntandoDeBundleSpec())
                .build();

        bundle.setApiVersion("entando.org/v1alpha1");
        return bundle;
    }
}
