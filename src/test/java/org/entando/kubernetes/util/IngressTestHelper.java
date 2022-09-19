package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoBaseCustomResource;
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
        return client.network().v1().ingresses().inNamespace(namespace).create(appIngress);
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
        return client.network().v1().ingresses().inNamespace(namespace).create(appIngress);
    }

    public static Ingress createPluginIngressWithRule(KubernetesClient client, EntandoPlugin plugin, IngressRule rule) {

        String namespace = plugin.getMetadata().getNamespace();
        String name = plugin.getMetadata().getName();
        Ingress appIngress = new IngressBuilder()
                .withNewMetadata()
                .withName(name + "-ingress")
                .withNamespace(namespace)
                .addToLabels(plugin.getKind(), name)
                .endMetadata()
                .withNewSpec()
                .addToRules(rule)
                .endSpec()
                .build();
        return client.network().v1().ingresses().inNamespace(namespace).create(appIngress);
    }

    public static Ingress getIngressForEntandoResource(EntandoBaseCustomResource ebcr) {
        String namespace = ebcr.getMetadata().getNamespace();
        String name = ebcr.getMetadata().getName();
        return new IngressBuilder()
                .withNewMetadata()
                .withName(name + "-ingress")
                .withNamespace(namespace)
                .addToLabels(ebcr.getKind(), name)
                .endMetadata()
                .build();
    }
}
