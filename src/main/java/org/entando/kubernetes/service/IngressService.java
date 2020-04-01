package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.extensions.DoneableIngress;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.ObservedNamespaces;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;

@Slf4j
public class IngressService {

    private KubernetesClient client;
    private ObservedNamespaces observedNamespaces;

    public IngressService(KubernetesClient client,
            ObservedNamespaces observedNamespaces) {

        this.client = client;
        this.observedNamespaces = observedNamespaces;
    }

    public Optional<Ingress> findByEntandoApp(EntandoApp app) {
        List<Ingress> appIngresses = getIngressOperations()
                .inNamespace(app.getMetadata().getName())
                .withLabel(app.getKind(), app.getMetadata().getName())
                .list().getItems();
        return appIngresses.stream().findFirst();
    }

    public Optional<Ingress> findByEntandoPlugin(EntandoPlugin plugin) {
        List<Ingress> appIngresses = getIngressOperations()
                .inNamespace(plugin.getMetadata().getName())
                .withLabel(plugin.getKind(), plugin.getMetadata().getName())
                .list().getItems();
        return appIngresses.stream().findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<Ingress, IngressList, DoneableIngress, Resource<Ingress, DoneableIngress>> getIngressOperations() {
        //CHECKSTYLE:ON
        return client.extensions().ingresses();
    }

}
