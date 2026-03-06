package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder;
import io.fabric8.kubernetes.api.model.networking.v1.IngressList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoMultiTenancy;
import org.entando.kubernetes.model.common.ServerStatus;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IngressService {
    
    public static final String ENTANDO_TENANTS_LABEL = "EntandoTenant";

    private KubernetesUtils kubernetesUtils;

    public IngressService(KubernetesUtils kubernetesUtils) {
        this.kubernetesUtils = kubernetesUtils;
    }
    
    public Optional<Ingress> findByEntandoApp(EntandoApp app, String tenantCode) {
        List<Ingress> appIngresses = null;
        if (StringUtils.isBlank(tenantCode) || EntandoMultiTenancy.PRIMARY_TENANT.equalsIgnoreCase(tenantCode)) {
            appIngresses = getIngressOperations()
                    .inNamespace(app.getMetadata().getNamespace())
                    .withLabel(app.getKind(), app.getMetadata().getName())
                    .withoutLabel(ENTANDO_TENANTS_LABEL)
                    .list().getItems();

        } else {
            appIngresses = getIngressOperations()
                    .inNamespace(app.getMetadata().getNamespace())
                    .withLabels(Map.of(app.getKind(), app.getMetadata().getName(), ENTANDO_TENANTS_LABEL, tenantCode))
                    .list().getItems();

        }
        if (appIngresses.size() > 1) {
            log.warn("Extracted more than one app ingress - names '{}'", 
                    appIngresses.stream().map(i -> i.getMetadata().getName()).collect(Collectors.joining(",")));
        }
        return appIngresses.stream().findFirst();
    }

    public Optional<Ingress> findByEntandoPlugin(EntandoPlugin plugin) {
        List<Ingress> appIngresses = getIngressOperations()
                .inNamespace(plugin.getMetadata().getNamespace())
                .withLabel(plugin.getKind(), plugin.getMetadata().getName())
                .list().getItems();
        return appIngresses.stream().findFirst();
    }

    public Map<String, Boolean> deletePathFromIngressByEntandoPlugin(EntandoPlugin plugin,
            List<EntandoAppPluginLink> links) {
        final Map<String, Boolean> results = new HashMap<>();
        links.stream().forEach(link -> {
            String ingressName = link.getStatus().getServerStatus("main")
                    .flatMap(ServerStatus::getIngressName)
                    .orElse(null);

            if (StringUtils.isNotBlank(ingressName)) {
                log.info("Ingress:'{}' path to remove in namespace:'{}' for pluginName:'{}'",
                        ingressName,
                        plugin.getMetadata().getNamespace(),
                        plugin.getMetadata().getName());

                PathIngressRemover editor = new PathIngressRemover(plugin.getMetadata().getNamespace(),
                        ingressName, getIngressOperations());
                String canonicalIngressPath = plugin.getSpec().getIngressPath();
                String customIngressPath = plugin.getSpec().getCustomIngressPath();

                Ingress res = editor.removeHttpPath(Arrays.asList(canonicalIngressPath, customIngressPath));
                results.put(ingressName, res != null);
            } else {
                log.info("No ingress path to remove in namespace:'{}' for pluginName:'{}'",
                        plugin.getMetadata().getNamespace(), plugin.getMetadata().getName());
            }
        });
        return results;
    }

    //CHECKSTYLE:OFF
    private MixedOperation<Ingress, IngressList, Resource<Ingress>> getIngressOperations() {
        //CHECKSTYLE:ON
        return kubernetesUtils.getCurrentKubernetesClient().network().v1().ingresses();
    }

    public static class PathIngressRemover {

        private final UnaryOperator<Ingress> action;
        private final IngressBuilder builder;

        public PathIngressRemover(String namespace, String ingressName,
                MixedOperation<Ingress, IngressList, Resource<Ingress>> ingressOperations) {
            Resource<Ingress> ingressResource = ingressOperations
                    .inNamespace(namespace)
                    .withName(ingressName);

            this.builder = new IngressBuilder(ingressResource.get());
            this.action = ingressResource::patch;

        }

        private Ingress done() {
            try {
                return action.apply(builder.build());
            } catch (Exception ex) {
                log.error("error editing ingress:'{}'", builder.buildMetadata().getName(), ex);
                return null;
            }

        }

        public Ingress removeHttpPath(List<String> httpPaths) {
            boolean isModified = false;
            // 1. Apply all changes to the builder in memory
            for (String httpPath : httpPaths) {
                if (httpPath == null) {
                    continue;
                }
                log.debug("Try to remove path:'{}' from Ingress:'{}'", httpPath, builder.buildMetadata().getName());
                // Find the path object safely
                var pathObject = builder.buildSpec().getRules().get(0).getHttp().getPaths()
                        .stream()
                        .filter(p -> StringUtils.equals(p.getPath(), httpPath))
                        .findFirst()
                        .orElse(null);

                if (pathObject != null) {
                    String annotationPathKey = retrieveAnnotationKeyFromPath(builder.buildMetadata().getAnnotations(),
                            pathObject.getPath());
                    // Apply edits for this path
                    builder.editSpec()
                            .editFirstRule()
                            .editHttp()
                            .removeFromPaths(pathObject) // Remove from list
                            .endHttp()
                            .endRule()
                            .endSpec()
                            .editMetadata()
                            .removeFromAnnotations(annotationPathKey)
                            .endMetadata();

                    isModified = true;
                }
            }
            // 2. Commit changes to the server ONLY ONCE
            if (isModified) {
                return this.done();
            } else {
                return null;
            }
        }

        private String retrieveAnnotationKeyFromPath(Map<String, String> annotations, String path) {
            return annotations.keySet().stream()
                    .filter(k -> StringUtils.equals(annotations.get(k), path))
                    .findFirst().orElse(null);
        }
    }
}
