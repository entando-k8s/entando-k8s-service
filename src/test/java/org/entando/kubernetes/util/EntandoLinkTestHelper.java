package org.entando.kubernetes.util;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import java.io.IOException;
import java.util.List;
import org.entando.kubernetes.model.link.DoneableEntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkBuilder;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkList;
import org.springframework.core.io.ClassPathResource;

public class EntandoLinkTestHelper {


    public static EntandoAppPluginLink createTestEntandoAppPluginLink(KubernetesClient client) {
        EntandoAppPluginLink el = getTestLink();
        KubernetesDeserializer.registerCustomKind(el.getApiVersion(), el.getKind(), EntandoAppPluginLink.class);
        return getEntandoAppPluginLinkOperations(client).inNamespace(el.getMetadata().getNamespace()).createOrReplace(el);

    }

    public static MixedOperation<EntandoAppPluginLink, EntandoAppPluginLinkList, DoneableEntandoAppPluginLink,
            Resource<EntandoAppPluginLink, DoneableEntandoAppPluginLink>> getEntandoAppPluginLinkOperations(KubernetesClient client) {
        CustomResourceDefinition linkCrd = createEntandoAppPluginLinkCrd(client);

        return client.customResources(linkCrd, EntandoAppPluginLink.class, EntandoAppPluginLinkList.class,
                DoneableEntandoAppPluginLink.class);
    }

    public static CustomResourceDefinition createEntandoAppPluginLinkCrd(KubernetesClient client) {
        String linkCrdFile = "crd/EntandoAppPluginLinkCRD.yaml";
        CustomResourceDefinition linkCrd = client.customResourceDefinitions().withName(EntandoAppPluginLink.CRD_NAME)
                .get();
        if (linkCrd == null) {
            List<HasMetadata> list = null;
            try {
                list = client.load(new ClassPathResource(linkCrdFile).getInputStream())
                        .get();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException("An error occurred while reading resource " + linkCrdFile, e);
            }
            linkCrd = (CustomResourceDefinition) list.get(0);
            // see issue https://github.com/fabric8io/kubernetes-client/issues/1486
            linkCrd.getSpec().getValidation().getOpenAPIV3Schema().setDependencies(null);
            return client.customResourceDefinitions().createOrReplace(linkCrd);
        }
        return linkCrd;
    }

    public static EntandoAppPluginLink getTestLink() {

        EntandoAppPluginLink link = new EntandoAppPluginLinkBuilder().withNewSpec()
                .withEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME)
                .withEntandoPlugin(TEST_PLUGIN_NAMESPACE, TEST_PLUGIN_NAME)
                .endSpec()
                .build();

        link.setMetadata(new ObjectMetaBuilder()
                .withName(String.format("%s-to-%s-link", TEST_APP_NAME, TEST_PLUGIN_NAME))
                .withNamespace(TEST_APP_NAMESPACE)
                .build());
        link.setApiVersion("entando.org/v1alpha1");
        return link;
    }
}
