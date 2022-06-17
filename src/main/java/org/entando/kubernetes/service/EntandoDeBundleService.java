package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.debundle.EntandoDeBundleBuilder;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService extends EntandoKubernetesResourceCollector<EntandoDeBundle> {

    public EntandoDeBundleService(KubernetesUtils kubernetesUtils,
            ObservedNamespaces observedNamespaces) {
        super(kubernetesUtils, observedNamespaces);
    }

    @Override
    protected List<EntandoDeBundle> getInAnyNamespace() {
        return getBundleOperations().inAnyNamespace().list().getItems();
    }

    @Override
    protected List<EntandoDeBundle> getInNamespaceWithoutChecking(String namespace) {
        return getBundleOperations().inNamespace(namespace).list().getItems();
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords) {
        return getAll().stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords) {
        return getAll().stream()
                .filter(b -> keywords.containsAll(b.getSpec().getDetails().getKeywords()))
                .collect(Collectors.toList());
    }

    public EntandoDeBundle createBundle(EntandoDeBundle entandoDeBundle) {
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        return getBundleOperations()
                .inNamespace(namespace).createOrReplace(entandoDeBundle);
    }

    public void deleteBundle(String bundleName) {

        if (ObjectUtils.isEmpty(bundleName)) {
            throw BadRequestExceptionFactory.invalidBundleNameRequest();
        }

        final EntandoDeBundle entandoDeBundle = new EntandoDeBundleBuilder().withNewMetadata().withName(bundleName)
                .endMetadata().build();
        String namespace = kubernetesUtils.getDefaultPluginNamespace();

        if (Boolean.FALSE.equals(
                getBundleOperations().inNamespace(namespace).delete(entandoDeBundle))) {

            throw NotFoundExceptionFactory.entandoDeBundle(bundleName);
        }

        log.info("Deleted {} EntandoDeBundle", bundleName);
    }

    //CHECKSTYLE:OFF
    private MixedOperation<EntandoDeBundle, KubernetesResourceList<EntandoDeBundle>, Resource<EntandoDeBundle>> getBundleOperations() {
        return getBundleOperations(kubernetesUtils.getCurrentKubernetesClient());
    }

    //CHECKSTYLE:OFF
    public static MixedOperation<EntandoDeBundle, KubernetesResourceList<EntandoDeBundle>, Resource<EntandoDeBundle>> getBundleOperations(
            KubernetesClient client) {
        return client.customResources(EntandoDeBundle.class);
    }

}
