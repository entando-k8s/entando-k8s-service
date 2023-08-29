package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.exception.BadRequestExceptionFactory;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.common.EntandoMultiTenancy;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EntandoDeBundleService extends EntandoKubernetesResourceCollector<EntandoDeBundle> {

    public static final String ENTANDO_TENANTS_ANNOTATION = "EntandoTenants";
    public static final String TENANTS_ANNOTATION_DELIMITER = ",";

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
        return getBundleOperations().inNamespace(namespace).list().getItems()
                .stream()
                .filter(entandoDeBundle -> isTenantPresent(entandoDeBundle, tenantCode))
                .collect(Collectors.toList());
    }

    public List<EntandoDeBundle> getAll(String tenantCode) {
        List<EntandoDeBundle> entandoDeBundles;
        if (observedNamespaces.isClusterScoped()) {
            entandoDeBundles = getInAnyNamespace()
                    .stream()
                    .filter(entandoDeBundle -> isTenantPresent(entandoDeBundle, tenantCode))
                    .collect(Collectors.toList());
        } else {
            entandoDeBundles = observedNamespaces.getNames().stream()
                    .flatMap(ns -> getAllInNamespace(ns, tenantCode).stream())
                    .collect(Collectors.toList());
        }
        return entandoDeBundles;
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
        List<String> tenantCodes = new ArrayList<>();
        findByName(entandoDeBundle.getMetadata().getName()).ifPresent(
                bundle -> {
                    List<String> fetchedTenantsList = fetchTenantsList(bundle);
                    if (fetchedTenantsList.isEmpty()) {
                        // management to add 'primary' code to pre-existing installations (before 7.3)
                        tenantCodes.add(EntandoMultiTenancy.PRIMARY_TENANT);
                    } else {
                        tenantCodes.addAll(fetchedTenantsList);
                    }
                }
        );
        if ((EntandoMultiTenancy.PRIMARY_TENANT.equals(tenantCode) && tenantCodes.isEmpty())
                || !isTenantPresent(tenantCodes, tenantCode)) {
            tenantCodes.add(tenantCode);
        }
        if (Objects.isNull(entandoDeBundle.getMetadata().getAnnotations())) {
            entandoDeBundle.getMetadata().setAnnotations(new HashMap<>());
        }
        entandoDeBundle.getMetadata().getAnnotations().put(ENTANDO_TENANTS_ANNOTATION,
                String.join(TENANTS_ANNOTATION_DELIMITER, tenantCodes));
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
                    removeTenantAnnotationFromBundle(entandoDeBundle, tenantCode);
                    if (fetchTenantsList(entandoDeBundle).isEmpty()) {
                        deleteBundleWithoutTenants(entandoDeBundle);
                    }

                },
                () -> {
                    throw NotFoundExceptionFactory.entandoDeBundle(bundleName);
                });

    }

    private boolean isTenantPresent(EntandoDeBundle entandoDeBundle, String tenantCode) {

        List<String> fetchedTenantsList = fetchTenantsList(entandoDeBundle);
        return isTenantPresent(fetchedTenantsList, tenantCode);
    }

    private boolean isTenantPresent(List<String> tenantCodes, String tenantCode) {
        if ((StringUtils.isBlank(tenantCode) || EntandoMultiTenancy.PRIMARY_TENANT.equals(tenantCode))
                && tenantCodes.isEmpty()) {
            return true;
        }
        return tenantCodes.stream()
                .anyMatch(e -> StringUtils.equals(e, tenantCode));
    }

    private List<String> fetchTenantsList(EntandoDeBundle entandoDeBundle) {
        return Optional.ofNullable(entandoDeBundle.getMetadata().getAnnotations())
                .map(annotations -> annotations.get(ENTANDO_TENANTS_ANNOTATION))
                .map(s -> Stream.of(s.split(TENANTS_ANNOTATION_DELIMITER))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private void removeTenantAnnotationFromBundle(EntandoDeBundle entandoDeBundle, String tenantCode) {
        String namespace = kubernetesUtils.getDefaultPluginNamespace();
        entandoDeBundle.getMetadata().getAnnotations().put(ENTANDO_TENANTS_ANNOTATION,
                fetchTenantsList(entandoDeBundle).stream()
                        .filter(e -> !StringUtils.equals(e, tenantCode))
                        .collect(Collectors.joining(TENANTS_ANNOTATION_DELIMITER)));
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
                .filter(entandoDeBundle -> Objects.nonNull(entandoDeBundle.getMetadata().getAnnotations()))
                .filter(entandoDeBundle -> entandoDeBundle.getMetadata().getName().equals(name))
                .filter(r -> containTenantAnnotationWithValue(r.getMetadata().getAnnotations(), tenantCode))
                .findFirst();
    }

    public Optional<EntandoDeBundle> findByName(String name, String tenantCode) {
        final String namespace = kubernetesUtils.getDefaultPluginNamespace();
        return findByNameAndNamespace(name, namespace, tenantCode);
    }

    private boolean containTenantAnnotationWithValue(Map<String, String> annotations, String tenantCode) {
        String values = annotations.get(ENTANDO_TENANTS_ANNOTATION);
        return values != null && Stream.of(values.split(TENANTS_ANNOTATION_DELIMITER))
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
