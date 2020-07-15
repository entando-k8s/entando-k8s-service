package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import java.io.IOException;
import java.util.List;
import org.entando.kubernetes.model.bundle.DoneableEntandoComponentBundle;
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleBuilder;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleList;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleSpec;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleSpecBuilder;
import org.springframework.core.io.ClassPathResource;

public class EntandoComponentBundleTestHelper {

    public static final String BASE_BUNDLES_ENDPOINT = "/bundles";
    public static final String TEST_BUNDLE_NAME = "my-bundle";
    public static final String TEST_BUNDLE_NAMESPACE = "test-namespace";

    public static EntandoComponentBundle createTestEntandoComponentBundle(KubernetesClient client) {
        EntandoComponentBundle eb = getTestEntandoComponentBundle();

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoComponentBundle.class);

        return getEntandoComponentBundleOperations(client).inNamespace(eb.getMetadata().getNamespace()).createOrReplace(eb);

    }

    public static EntandoComponentBundle createTestEntandoComponentBundleInNamespace(KubernetesClient client, String namespace) {
        EntandoComponentBundle eb = getTestEntandoComponentBundle();
        eb.getMetadata().setNamespace(namespace);

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoComponentBundle.class);

        return getEntandoComponentBundleOperations(client).inNamespace(namespace).createOrReplace(eb);

    }

    public static MixedOperation<EntandoComponentBundle, EntandoComponentBundleList, DoneableEntandoComponentBundle,
            Resource<EntandoComponentBundle, DoneableEntandoComponentBundle>> getEntandoComponentBundleOperations(KubernetesClient client) {
        CustomResourceDefinition entandoDeBundleCrd = createEntandoComponentBundleCrd(client);

        return client.customResources(entandoDeBundleCrd, EntandoComponentBundle.class, EntandoComponentBundleList.class,
                DoneableEntandoComponentBundle.class);
    }

    public static CustomResourceDefinition createEntandoComponentBundleCrd(KubernetesClient client) {
        String entandoDeBundleCrdResource = "crd/EntandoComponentBundleCRD.yaml";
        CustomResourceDefinition entandoDeBundleCrd = client.customResourceDefinitions().withName(EntandoComponentBundle.CRD_NAME)
                .get();
        if (entandoDeBundleCrd == null) {
            List<HasMetadata> list = null;
            try {
                list = client.load(new ClassPathResource(entandoDeBundleCrdResource).getInputStream())
                        .get();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("An error occurred while reading resource " + entandoDeBundleCrdResource, e);
            }
            entandoDeBundleCrd = (CustomResourceDefinition) list.get(0);
            // see issue https://github.com/fabric8io/kubernetes-client/issues/1486
            entandoDeBundleCrd.getSpec().getValidation().getOpenAPIV3Schema().setDependencies(null);
            return client.customResourceDefinitions().createOrReplace(entandoDeBundleCrd);
        }
        return entandoDeBundleCrd;
    }

    public static EntandoComponentBundleSpec getTestEntandoComponentBundleSpec() {
        return new EntandoComponentBundleSpecBuilder()
                .withDescription("A bundle containing some demo components for Entando6")
                .withCode("inail_bundle")
                .addNewVersion()
                .withVersion("0.0.1")
                .withIntegrity(
                        "sha512-n4TEroSqg/sZlEGg2xj6RKNtl/t3ZROYdNd99/dl3UrzCUHvBrBxZ1rxQg/sl3kmIYgn3+ogbIFmUZYKWxG3Ag==")
                .withUrl("http://localhost:8081/repository/npm-internal/inail_bundle/-/inail_bundle-0.0.1.tgz")
                .endVersion()
                .build();
    }

    public static EntandoComponentBundle getTestEntandoComponentBundle() {

        EntandoComponentBundle bundle = new EntandoComponentBundleBuilder()
                .withNewMetadata()
                    .withName(TEST_BUNDLE_NAME)
                    .withNamespace(TEST_BUNDLE_NAMESPACE)
                .endMetadata()
                .withSpec(getTestEntandoComponentBundleSpec())
                .build();

        bundle.setApiVersion("entando.org/v1alpha1");
        return bundle;
    }
}
