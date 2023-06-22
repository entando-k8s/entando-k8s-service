package org.entando.kubernetes.util;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.Map;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoBaseCustomResource;
import org.entando.kubernetes.model.plugin.EntandoPlugin;

public class IngressTestHelper {
    
    public static Ingress createAppIngress(KubernetesClient client, EntandoApp app) {
        return IngressTestHelper.createAppIngress(client, app, "-ingress", Map.of());
    }

    public static Ingress createAppIngress(KubernetesClient client, EntandoApp app, String nameSuffix, Map<String, String> labels) {
        String namespace = app.getMetadata().getNamespace();
        String name = app.getMetadata().getName();
        Ingress appIngress = new IngressBuilder()
                .withNewMetadata()
                .withName(name + nameSuffix)
                .withNamespace(namespace)
                .addToLabels(app.getKind(), name)
                .addToLabels(labels)
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

    public static Ingress createPluginIngressWithRuleAndAnnotation(KubernetesClient client, EntandoPlugin plugin,
            IngressRule rule, Map<String, String> annotations) {

        String namespace = plugin.getMetadata().getNamespace();
        String name = plugin.getMetadata().getName();
        Ingress appIngress = new IngressBuilder()
                .withNewMetadata()
                .withName(name + "-ingress")
                .withNamespace(namespace)
                .addToLabels(plugin.getKind(), name)
                .addToAnnotations(annotations)
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
