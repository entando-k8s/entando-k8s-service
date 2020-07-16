package org.entando.kubernetes.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.model.bundle.DoneableEntandoComponentBundle;
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleList;
import org.entando.kubernetes.model.bundle.EntandoComponentBundleOperationFactory;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoComponentBundleService extends EntandoKubernetesResourceCollector<EntandoComponentBundle>{

    public EntandoComponentBundleService(KubernetesClient client,
            ObservedNamespaces observedNamespaces) {
        super(client, observedNamespaces);
    }

    @Override
    protected List<EntandoComponentBundle> getInNamespaceWithoutChecking(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoComponentBundle, EntandoComponentBundleList, DoneableEntandoComponentBundle, Resource<EntandoComponentBundle, DoneableEntandoComponentBundle>> getBundleOperations() {
        //CHECKSTYLE:ON
        return EntandoComponentBundleOperationFactory.produceAllEntandoComponentBundles(client);
    }

}
