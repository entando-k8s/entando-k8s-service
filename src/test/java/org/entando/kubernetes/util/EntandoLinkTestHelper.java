package org.entando.kubernetes.util;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkBuilder;
import org.entando.kubernetes.service.EntandoLinkService;

public class EntandoLinkTestHelper {

    public static EntandoAppPluginLink createTestEntandoAppPluginLink(KubernetesClient client) {
        EntandoAppPluginLink el = getTestLink();
        KubernetesDeserializer.registerCustomKind(el.getApiVersion(), el.getKind(), EntandoAppPluginLink.class);
        return EntandoLinkService.getLinksOperations(client)
                .inNamespace(el.getMetadata().getNamespace()).createOrReplace(el);
    }

    public static void deleteInAllNamespaces(KubernetesClient client) {
        EntandoAppPluginLink el = getTestLink();
        KubernetesDeserializer.registerCustomKind(el.getApiVersion(), el.getKind(), EntandoAppPluginLink.class);
        EntandoLinkService.getLinksOperations(client).inAnyNamespace().delete();
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
