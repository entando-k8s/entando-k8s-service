package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService extends EntandoKubernetesResourceCollector<EntandoDeBundle> {

    public static final String ENTANDO_TENANTS_LABEL = "EntandoTenants";

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

    public List<EntandoDeBundle> getAllInNamespace(String namespace, String tenantCode) {
        return getBundleOperations()
                .inNamespace(namespace)
                .withLabelIn(ENTANDO_TENANTS_LABEL, tenantCode)
                .list().getItems();
    }

    public List<EntandoDeBundle> getAll(String tenantCode) {
        return getBundleOperations()
                .inAnyNamespace()
                .withLabel(ENTANDO_TENANTS_LABEL)
                .list().getItems().stream()
                .filter(entandoDeBundle -> isTenantPresent(entandoDeBundle, tenantCode))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAnyKeywords(List<String> keywords, String tenantCode) {
        return getAll(tenantCode).stream()
                .filter(b -> b.getSpec().getDetails().getKeywords().stream().anyMatch(keywords::contains))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> findBundlesByAllKeywords(List<String> keywords, String tenantCode) {
        return getAll(tenantCode).stream()
                .filter(b -> keywords.containsAll(b.getSpec().getDetails().getKeywords()))
                .collect(Collectors.toList());
    }

    public EntandoDeBundle createBundle(EntandoDeBundle entandoDeBundle, String tenantCode) {
        List<String> tenantsLabels = new ArrayList<>();
        findByName(entandoDeBundle.getMetadata().getName()).ifPresent(
                bundle -> tenantsLabels.addAll(fetchTenantsList(bundle))
        );
        if (!isTenantPresent(tenantsLabels, tenantCode)) {
            tenantsLabels.add(tenantCode);
        }

        entandoDeBundle.getMetadata().getLabels().put(ENTANDO_TENANTS_LABEL,
                tenantsLabels.stream().collect(Collectors.joining(",")));
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        return getBundleOperations()
                .inNamespace(namespace).createOrReplace(entandoDeBundle);
    }

    public void deleteBundle(String bundleName, String tenantCode) {

        if (ObjectUtils.isEmpty(bundleName)) {
            throw BadRequestExceptionFactory.invalidBundleNameRequest();
        }

        findByName(bundleName, tenantCode).ifPresentOrElse(
                entandoDeBundle -> {
                    removeTenantLabelFromBundle(entandoDeBundle, tenantCode);
                    if (fetchTenantsList(entandoDeBundle).isEmpty()) {
                        deleteBundleWithoutTenants(entandoDeBundle);
                    }

                },
                () -> {
                    throw NotFoundExceptionFactory.entandoDeBundle(bundleName);
                });

    }

    private boolean isTenantPresent(EntandoDeBundle entandoDeBundle, String tenantCode) {
        return fetchTenantsList(entandoDeBundle).stream()
                .filter(e -> StringUtils.equals(e, tenantCode)).count() > 0;
    }

    private boolean isTenantPresent(List<String> tenantsLabels, String tenantCode) {
        return tenantsLabels.stream()
                .filter(e -> StringUtils.equals(e, tenantCode)).count() > 0;
    }

    private List<String> fetchTenantsList(EntandoDeBundle entandoDeBundle) {
        return Stream.of(entandoDeBundle.getMetadata().getLabels().get(ENTANDO_TENANTS_LABEL).split(","))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private void removeTenantLabelFromBundle(EntandoDeBundle entandoDeBundle, String tenantCode) {
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        entandoDeBundle.getMetadata().getLabels().put(ENTANDO_TENANTS_LABEL,
                fetchTenantsList(entandoDeBundle).stream()
                        .filter(e -> !StringUtils.equals(e, tenantCode))
                        .collect(Collectors.joining(",")));
        getBundleOperations()
                .inNamespace(namespace).createOrReplace(entandoDeBundle);
    }

    private void deleteBundleWithoutTenants(EntandoDeBundle entandoDeBundle) {
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        Boolean deleted = getBundleOperations().inNamespace(namespace).delete(entandoDeBundle);
        log.info("Deleted EntandoDeBundle with name:'{}' ? '{}'", entandoDeBundle.getMetadata().getName(), deleted);
    }

    public Optional<EntandoDeBundle> findByNameAndNamespace(String name, String namespace, String tenantCode) {
        return getAllInNamespace(namespace)
                .stream()
                .filter(r -> r.getMetadata().getName().equals(name))
                .filter(r -> containTenantLabelWithValue(r.getMetadata().getLabels(), tenantCode))
                .findFirst();
    }

    public Optional<EntandoDeBundle> findByName(String name, String tenantCode) {

        final String namespace = kubernetesUtils.getDefaultPluginNamespace();
        return getAllInNamespace(namespace)
                .stream()
                .filter(r -> r.getMetadata().getName().equals(name))
                .filter(r -> containTenantLabelWithValue(r.getMetadata().getLabels(), tenantCode))
                .findFirst();
    }

    private boolean containTenantLabelWithValue(Map<String, String> labels, String tenantCode) {
        String values = labels.get(ENTANDO_TENANTS_LABEL);
        return values != null && Stream.of(values.split(","))
                .map(StringUtils::trim)
                .map(StringUtils::strip)
                .anyMatch(v -> StringUtils.equals(v, tenantCode));
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
