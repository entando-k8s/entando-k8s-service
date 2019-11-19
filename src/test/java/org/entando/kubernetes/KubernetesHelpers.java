package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatusBuilder;
import java.util.Arrays;
import org.entando.kubernetes.model.*;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;
import org.entando.kubernetes.model.plugin.EntandoPluginSpecBuilder;

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

        EntandoPluginSpec pluginSpec = ((EntandoPluginSpecBuilder) new EntandoPluginSpecBuilder()
                .withHealthCheckPath("/actuator/health")
                .withImage("entando/entando-plugin-image")
                .addNewRole("read", "Read")
                .addNewPermission("another-client", "read")
                .withIngressPath("/pluginpath")
                .withReplicas(1)
                .withDbms(DbmsImageVendor.MYSQL)).build();


        EntandoCustomResourceStatus pluginStatus = new EntandoCustomResourceStatus();
        pluginStatus.setEntandoDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL);

        DbServerStatus pluginDbStatus = new DbServerStatus("db");

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

        pluginDbStatus.setPersistentVolumeClaimStatuses(Arrays.asList(
                new PersistentVolumeClaimStatusBuilder()
                        .withPhase("Bound")
                        .build()));

        WebServerStatus pluginServerStatus = new WebServerStatus("server");

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

        pluginStatus.putServerStatus(pluginDbStatus);
        pluginStatus.putServerStatus(pluginServerStatus);

        plugin.setSpec(pluginSpec);
        plugin.setMetadata(pluginMeta);
        plugin.setStatus(pluginStatus);
        return plugin;
    }
}
