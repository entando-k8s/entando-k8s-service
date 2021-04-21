package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.app.DoneableEntandoApp;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.app.EntandoAppList;
import org.entando.kubernetes.model.app.EntandoAppOperationFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoAppService extends EntandoKubernetesResourceCollector<EntandoApp> {

    public EntandoAppService(KubernetesUtils kubernetesUtils,
            ObservedNamespaces observedNamespaces) {
        super(kubernetesUtils, observedNamespaces);
    }

    @Override
    protected List<EntandoApp> getInAnyNamespace() {
        return getEntandoAppsOperations().inAnyNamespace().list().getItems();
    }

    @Override
    protected List<EntandoApp> getInNamespaceWithoutChecking(String namespace) {
        return getEntandoAppsOperations().inNamespace(namespace).list().getItems();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoApp, EntandoAppList, DoneableEntandoApp, Resource<EntandoApp, DoneableEntandoApp>> getEntandoAppsOperations() {
        //CHECKSTYLE:ON
        return EntandoAppOperationFactory.produceAllEntandoApps(kubernetesUtils.getCurrentKubernetesClient());
    }

}
