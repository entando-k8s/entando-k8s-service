package org.entando.kubernetes.service;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entando.kubernetes.exception.PluginNotFoundException;
import org.entando.kubernetes.model.DeploymentStatus;
import org.entando.kubernetes.model.EntandoPluginDeploymentRequest;
import org.entando.kubernetes.model.EntandoPluginDeploymentResponse;
import org.entando.kubernetes.model.PluginServiceStatus;
import org.entando.kubernetes.model.PodStatus;
import org.entando.kubernetes.model.plugin.AbstractServerStatus;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoCustomResourceStatus;
import org.entando.kubernetes.model.plugin.EntandoDeploymentPhase;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;
import org.entando.kubernetes.service.digitalexchange.model.DigitalExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class KubernetesService {

    public static final String ENTANDOPLUGIN_CRD_NAME = "entandoplugins.entando.org";

    private final @NonNull KubernetesClient client;
    private final @NonNull String namespace;
    private final @NonNull String entandoAppName;

    public KubernetesService(@Autowired final KubernetesClient client,
                             @Value("${org.entando.kubernetes.namespace}") final String namespace,
                             @Value("${org.entando.kubernetes.app-name}") final String entandoAppName) {
        this.client = client;
        this.namespace = namespace;
        this.entandoAppName = entandoAppName;
    }

    public EntandoPluginDeploymentResponse getDeployment(final String pluginId) {
        return getDeploymentOptional(pluginId)
                .orElseThrow(PluginNotFoundException::new);
    }

    public Optional<EntandoPluginDeploymentResponse> getDeploymentOptional(final String pluginId) {
        return ofNullable(getOperation().withName(pluginId).get())
                .map(this::map);
    }

    public void deleteDeployment(final String pluginId) {
        final NonNamespaceOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin,
                Resource<EntandoPlugin, DoneableEntandoPlugin>> operation = getOperation();
        ofNullable(operation.withName(pluginId).get())
                .ifPresent(operation::delete);
    }

    public List<EntandoPluginDeploymentResponse> getDeployments() {
        return getOperation().list()
                .getItems().stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public void deploy(final EntandoPluginDeploymentRequest request) {
        final EntandoPlugin plugin = new EntandoPlugin();
        final EntandoPluginSpec spec = new EntandoPluginSpec();
        final ObjectMeta objectMeta = new ObjectMeta();

        objectMeta.setName(request.getPlugin());

        spec.setDbms(request.getDbms());
        spec.setImage(request.getImage());
        spec.setRoles(request.getRoles());
        spec.setPermissions(request.getPermissions());
        spec.setIngressPath(request.getIngressPath());
        spec.setHealthCheckPath(request.getHealthCheckPath());
        spec.setEntandoAppName(entandoAppName);
        spec.setReplicas(1);

        plugin.setMetadata(objectMeta);
        plugin.setSpec(spec);
        plugin.setApiVersion("entando.org/v1alpha1");

        getOperation().create(plugin);
    }

    private EntandoPluginDeploymentResponse map(final EntandoPlugin deployment) {
        final String pluginId = deployment.getMetadata().getName();
        final EntandoPluginDeploymentResponse response = new EntandoPluginDeploymentResponse();
        final EntandoCustomResourceStatus entandoStatus = deployment.getSpec().getEntandoStatus();
        final EntandoDeploymentPhase deploymentPhase = ofNullable(entandoStatus)
                .map(EntandoCustomResourceStatus::getEntandoDeploymentPhase)
                .orElse(EntandoDeploymentPhase.STARTED);

        response.setPath(deployment.getSpec().getIngressPath());
        response.setPlugin(pluginId);
        response.setReplicas(deployment.getSpec().getReplicas());
        response.setDeploymentPhase(deploymentPhase.toValue());

        ofNullable(entandoStatus)
                .map(EntandoCustomResourceStatus::getJeeServerStatus)
                .filter(list -> !list.isEmpty())
                .map(list -> convert(list.get(0), "jeeServer"))
                .ifPresent(response::setServerStatus);

        ofNullable(entandoStatus)
                .map(EntandoCustomResourceStatus::getDbServerStatus)
                .map(list -> list.stream().map(item -> convert(item, "dbServer")).collect(Collectors.toList()))
                .ifPresent(response::setExternalServiceStatuses);

        final Integer availableReplicas = ofNullable(response.getServerStatus())
                .map(PluginServiceStatus::getDeploymentStatus)
                .map(DeploymentStatus::getAvailableReplicas)
                .orElse(0);
        response.setOnline(EntandoDeploymentPhase.SUCCESSFUL.equals(deploymentPhase) && availableReplicas > 0);

        return response;
    }

    private PluginServiceStatus convert(final AbstractServerStatus status, final String type) {
        final PluginServiceStatus serviceStatus = new PluginServiceStatus();
        final PodStatus podStatus = new PodStatus();
        final DeploymentStatus deploymentStatus = new DeploymentStatus();

        serviceStatus.setReplicas(status.getDeploymentStatus().getReplicas());
        serviceStatus.setType(type);
        serviceStatus.setVolumePhase(status.getPersistentVolumeClaimStatus().getPhase());
        serviceStatus.setPodStatus(podStatus);
        serviceStatus.setDeploymentStatus(deploymentStatus);

        podStatus.setPhase(status.getPodStatus().getPhase());
        podStatus.setContainerStatuses(status.getPodStatus().getInitContainerStatuses());
        podStatus.setConditions(sort(status.getPodStatus().getConditions(), comparing(this::localDateTime)));

        deploymentStatus.setAvailableReplicas(status.getDeploymentStatus().getAvailableReplicas());
        deploymentStatus.setReadyReplicas(status.getDeploymentStatus().getReadyReplicas());
        deploymentStatus.setUpdatedReplicas(status.getDeploymentStatus().getUpdatedReplicas());
        deploymentStatus.setReplicas(status.getDeploymentStatus().getReplicas());
        deploymentStatus.setConditions(sort(status.getDeploymentStatus().getConditions(), comparing(this::localDateTime)));

        return serviceStatus;
    }

    private LocalDateTime localDateTime(final PodCondition condition) {
        return LocalDateTime.parse(condition.getLastTransitionTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private LocalDateTime localDateTime(final DeploymentCondition condition) {
        return LocalDateTime.parse(condition.getLastTransitionTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private <T> List<T> sort(final List<T> list, final Comparator<T> comparator) {
        final List<T> sortedList = new ArrayList<>(list);
        sortedList.sort(comparator);
        return sortedList;
    }

    private NonNamespaceOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> getOperation() {
        final CustomResourceDefinition entandoPluginCrd = client.customResourceDefinitions()
                .withName(ENTANDOPLUGIN_CRD_NAME).get();
        return client
                .customResources(entandoPluginCrd, EntandoPlugin.class, EntandoPluginList.class, DoneableEntandoPlugin.class)
                .inNamespace(namespace);
    }

}
