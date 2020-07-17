package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import java.io.IOException;
import java.util.List;
import org.entando.kubernetes.model.debundle.DoneableEntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleBuilder;
import org.entando.kubernetes.model.debundle.EntandoDeBundleList;
import org.entando.kubernetes.model.debundle.EntandoDeBundleSpec;
import org.entando.kubernetes.model.debundle.EntandoDeBundleSpecBuilder;
import org.springframework.core.io.ClassPathResource;

public class EntandoDeBundleTestHelper {

    public static final String BASE_BUNDLES_ENDPOINT = "/bundles";
    public static final String TEST_BUNDLE_NAME = "my-bundle";
    public static final String TEST_BUNDLE_NAMESPACE = "test-namespace";

    public static EntandoDeBundle createTestEntandoDeBundle(KubernetesClient client) {
        EntandoDeBundle eb = getTestEntandoDeBundle();

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoDeBundle.class);

        return getEntandoDeBundleOperations(client).inNamespace(eb.getMetadata().getNamespace()).createOrReplace(eb);

    }

    public static EntandoDeBundle createTestEntandoDeBundleInNamespace(KubernetesClient client, String namespace) {
        EntandoDeBundle eb = getTestEntandoDeBundle();
        eb.getMetadata().setNamespace(namespace);

        KubernetesDeserializer
                .registerCustomKind(eb.getApiVersion(), eb.getKind(), EntandoDeBundle.class);

        return getEntandoDeBundleOperations(client).inNamespace(namespace).createOrReplace(eb);

    }

    public static MixedOperation<EntandoDeBundle, EntandoDeBundleList, DoneableEntandoDeBundle,
            Resource<EntandoDeBundle, DoneableEntandoDeBundle>> getEntandoDeBundleOperations(KubernetesClient client) {
        CustomResourceDefinition entandoDeBundleCrd = createEntandoDeBundleCrd(client);

        return client.customResources(entandoDeBundleCrd, EntandoDeBundle.class, EntandoDeBundleList.class,
                DoneableEntandoDeBundle.class);
    }

    public static CustomResourceDefinition createEntandoDeBundleCrd(KubernetesClient client) {
        String entandoDeBundleCrdResource = "crd/EntandoDeBundleCRD.yaml";
        CustomResourceDefinition entandoDeBundleCrd = client.customResourceDefinitions().withName(EntandoDeBundle.CRD_NAME)
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
