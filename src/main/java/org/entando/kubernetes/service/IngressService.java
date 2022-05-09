package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IngressService {

    private KubernetesUtils kubernetesUtils;

    public IngressService(KubernetesUtils kubernetesUtils) {
        this.kubernetesUtils = kubernetesUtils;
    }

    public Optional<Ingress> findByEntandoApp(EntandoApp app) {
        List<Ingress> appIngresses = getIngressOperations()
                .inNamespace(app.getMetadata().getNamespace())
                .withLabel(app.getKind(), app.getMetadata().getName())
                .list().getItems();
        return appIngresses.stream().findFirst();
    }

    public Optional<Ingress> findByEntandoPlugin(EntandoPlugin plugin) {
        List<Ingress> appIngresses = getIngressOperations()
                .inNamespace(plugin.getMetadata().getNamespace())
                .withLabel(plugin.getKind(), plugin.getMetadata().getName())
                .list().getItems();
        return appIngresses.stream().findFirst();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<Ingress, IngressList, Resource<Ingress>> getIngressOperations() {
        //CHECKSTYLE:ON
        return kubernetesUtils.getCurrentKubernetesClient().network().v1().ingresses();
    }

}
