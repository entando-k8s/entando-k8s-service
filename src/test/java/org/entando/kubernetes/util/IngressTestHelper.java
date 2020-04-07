package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;

public class IngressTestHelper {

    public static Ingress createAppIngress(KubernetesClient client, EntandoApp app) {
        String namespace = app.getMetadata().getNamespace();
        String name = app.getMetadata().getName();
        Ingress appIngress = new IngressBuilder()
               .withNewMetadata()
                   .withName(name + "-ingress")
                   .withNamespace(namespace)
                   .addToLabels(app.getKind(), name)
               .endMetadata()
               .build();
       return client.extensions().ingresses().inNamespace(namespace).create(appIngress);
    }

    public static Ingress createPluginIngress(KubernetesClient client, EntandoPlugin plugin) {

        String namespace = plugin.getMetadata().getNamespace();
        String name = plugin.getMetadata().getName();
        Ingress appIngress = new IngressBuilder()
                .withNewMetadata()
                .withName(name + "-ingress")
                .withNamespace(namespace)
                .addToLabels(plugin.getKind(), name)
                .endMetadata()
                .build();
        return client.extensions().ingresses().inNamespace(namespace).create(appIngress);

    }
}
