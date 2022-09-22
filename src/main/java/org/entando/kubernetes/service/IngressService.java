package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import io.fabric8.kubernetes.api.model.networking.v1.IngressFluent;
import io.fabric8.kubernetes.api.model.networking.v1.IngressFluentImpl;
import io.fabric8.kubernetes.api.model.networking.v1.IngressList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.ServerStatus;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
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
        private final IngressFluent<?> fluent;

        public PathIngressRemover(String namespace, String ingressName,
                MixedOperation<Ingress, IngressList, Resource<Ingress>> ingressOperations) {
            Resource<Ingress> ingressResource = ingressOperations
                    .inNamespace(namespace)
                    .withName(ingressName);

            this.fluent = new IngressFluentImpl<>(ingressResource.get());
            this.action = ingressResource::patch;

        }

        private Ingress done() {
            Ingress built = new Ingress(fluent.getApiVersion(), fluent.getKind(), fluent.buildMetadata(),
                    fluent.buildSpec(), fluent.buildStatus());
            try {
                return action.apply(built);
            } catch (Exception ex) {
                log.error("error editing ingress:'{}'", fluent.buildMetadata().getName(), ex);
                return null;
            }

        }

        public Ingress removeHttpPath(List<String> httpPaths) {
            Ingress ingress = null;
            for (String httpPath : httpPaths) {
                ingress = Optional.ofNullable(httpPath).map(path -> {
                            log.debug("Try to remove path:'{}' from Ingress:'{}'", path, fluent.buildMetadata().getName());
                            return fluent.buildSpec().getRules().get(0).getHttp().getPaths()
                                    .stream()
                                    .filter(p -> StringUtils.equals(p.getPath(), path))
                                    .findFirst().orElse(null);
                        }
                ).map(p -> {
                    String annotationPathKey = retrieveAnnotationKeyFromPath(fluent.buildMetadata().getAnnotations(),
                            p.getPath());
                    fluent.editSpec().editFirstRule().editHttp()
                            .removeFromPaths(p)
                            .endHttp()
                            .endRule()
                            .endSpec()
                            .editMetadata().removeFromAnnotations(annotationPathKey).endMetadata();
                    return this.done();

                }).orElse(null);
            }
            return ingress;
        }

        private String retrieveAnnotationKeyFromPath(Map<String, String> annotations, String path) {
            return annotations.keySet().stream()
                    .filter(k -> StringUtils.equals(annotations.get(k), path))
                    .findFirst().orElse(null);
        }
    }
}
