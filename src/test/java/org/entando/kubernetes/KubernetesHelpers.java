package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatusBuilder;
import org.entando.kubernetes.model.*;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;

public class KubernetesHelpers {

    public static DeploymentCondition createDeploymentCondition( String ts,  String message,
                                                        String reason,  String type) {
        DeploymentCondition condition = new DeploymentCondition();
        condition.setLastTransitionTime(ts);
        condition.setLastUpdateTime(ts);
        condition.setMessage(message);
        condition.setReason(reason);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }

    public static PodCondition createPodCondition( String ts,  String type) {
        PodCondition condition = new PodCondition();
        condition.setLastTransitionTime(ts);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }

    public static EntandoPlugin getTestEntandoPlugin() {
        EntandoPlugin plugin = new EntandoPlugin();

        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName("plugin-name").withNamespace("plugin-namespace").build();

        EntandoPluginSpec pluginSpec = new EntandoPluginSpec.EntandoPluginSpecBuilder()
                .withEntandoApp("entando-app-namespace", "entando-app")
                .withIngressPath("/pluginpath")
                .withReplicas(1)
                .withKeycloakServer("keycloak-namespace", "keycloak-server")
                .withDbms(DbmsImageVendor.MYSQL)
                .withHealthCheckPath("/actuator/health")
                .withImage("entando/entando-plugin-image")
                .addRole("read", "Read")
                .addPermission("another-client", "read")
                .build();


        EntandoCustomResourceStatus pluginStatus = new EntandoCustomResourceStatus();
        pluginStatus.setEntandoDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL);

        DbServerStatus pluginDbStatus = new DbServerStatus();

        pluginDbStatus.setPodStatus
                (new PodStatusBuilder()
                        .withPhase("Running")
                        .addToConditions(createPodCondition("2019-07-11T18:36:09Z", "Available"))
                        .addToConditions(createPodCondition("2019-07-11T18:36:06Z", "Initialized"))
                        .build());
        pluginDbStatus.setDeploymentStatus(
                new DeploymentStatusBuilder()
                        .addToConditions(createDeploymentCondition("2019-07-11T18:36:06Z", "Some message",
                                "MinimumReplicasAvailable", "Available"))
                        .addToConditions(createDeploymentCondition("2019-07-11T18:36:03Z", "Some message",
                                "NewReplicaSetAvailable", "Progressing"))
                        .build());

        pluginDbStatus.setPersistentVolumeClaimStatus(
                new PersistentVolumeClaimStatusBuilder()
                        .withPhase("Bound")
                        .build());

        JeeServerStatus pluginServerStatus = new JeeServerStatus();

        pluginServerStatus.setPodStatus(
                new PodStatusBuilder()
                        .withPhase("Running")
                        .addToConditions(createPodCondition("2019-07-11T18:36:06Z", "Initialized"))
                        .build());

        pluginServerStatus.setDeploymentStatus(
                new DeploymentStatusBuilder()
                        .addToConditions(createDeploymentCondition("2019-07-11T18:36:06Z", "Some message",
                                "NewReplicaSetAvailable", "Progressing"))
                        .build());

        pluginStatus.addDbServerStatus(pluginDbStatus);
        pluginStatus.addJeeServerStatus(pluginServerStatus);

        plugin.setSpec(pluginSpec);
        plugin.setMetadata(pluginMeta);
        plugin.setStatus(pluginStatus);
        return plugin;
    }
}
