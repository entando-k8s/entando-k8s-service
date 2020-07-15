package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;

import java.io.IOException;
import java.util.List;

import org.entando.kubernetes.model.bundle.*;
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
        CustomResourceDefinition entandoComponentBundleCrd = createEntandoComponentBundleCrd(client);

        return client.customResources(entandoComponentBundleCrd, EntandoComponentBundle.class, EntandoComponentBundleList.class,
                DoneableEntandoComponentBundle.class);
    }

    public static CustomResourceDefinition createEntandoComponentBundleCrd(KubernetesClient client) {
        String entandoComponentBundleCrdResource = "crd/EntandoComponentBundleCRD.yaml";
        CustomResourceDefinition entandoComponentBundleCrd = client.customResourceDefinitions().withName(EntandoComponentBundle.CRD_NAME)
                .get();
        if (entandoComponentBundleCrd == null) {
            List<HasMetadata> list = null;
            try {
                list = client.load(new ClassPathResource(entandoComponentBundleCrdResource).getInputStream())
                        .get();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("An error occurred while reading resource " + entandoComponentBundleCrdResource, e);
            }
            entandoComponentBundleCrd = (CustomResourceDefinition) list.get(0);
            // see issue https://github.com/fabric8io/kubernetes-client/issues/1486
            entandoComponentBundleCrd.getSpec().getValidation().getOpenAPIV3Schema().setDependencies(null);
            return client.customResourceDefinitions().createOrReplace(entandoComponentBundleCrd);
        }
        return entandoComponentBundleCrd;
    }


    public static EntandoComponentBundleSpec getTestEntandoComponentBundleSpec() {
        return new EntandoComponentBundleSpecBuilder()
                .withCode("inail_bundle")
                .withTitle("INAIL bundle")
                .withDescription("A bundle containing some demo components for Entando6")
                .withNewAuthor()
                .withName("KEB TEAM")
                .withEmail("keb@entando.com")
                .endAuthor()
                .withOrganization("inail.gov")
                .withNewImages()
                .addImageUrl("http://www.images.com/img2.jpg")
                .addImageUrl("http://www.images.com/img6.jpg")
                .endImages()
                .withUrl("http://www.github.com/entandone/bundle1")
                .withThumbnail("base64-image")
                .addNewVersion()
                .withVersion("v0.0.1")
                .withIntegrity("signed")
                .withTimestamp("2020-06-29T16:17:00.000Z")
                .endVersion()
                .addNewVersion()
                .withVersion("v2.0.0")
                .withIntegrity("tag")
                .withTimestamp("2020-07-01T08:56:00.000Z")
                .endVersion()
                .build();
    }

    // TODO do a test also withNoVersions()!

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
