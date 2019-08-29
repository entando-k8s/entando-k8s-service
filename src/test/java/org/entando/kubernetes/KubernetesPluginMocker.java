package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimStatus;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import org.entando.kubernetes.model.plugin.DbServerStatus;
import org.entando.kubernetes.model.plugin.EntandoCustomResourceStatus;
import org.entando.kubernetes.model.plugin.EntandoDeploymentPhase;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;
import org.entando.kubernetes.model.plugin.JeeServerStatus;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

public class KubernetesPluginMocker {

    @Mock public EntandoPlugin plugin;
    @Mock public ObjectMeta metadata;
    @Mock public EntandoPluginSpec spec;
    @Mock public EntandoCustomResourceStatus entandoStatus;
    @Mock public JeeServerStatus jeeServerStatus;
    @Mock public DbServerStatus dbServerStatus;
    @Mock public DeploymentStatus jeeDeploymentStatus;
    @Mock public DeploymentStatus dbDeploymentStatus;
    @Mock public PersistentVolumeClaimStatus dbPvcStatus;
    @Mock public PersistentVolumeClaimStatus jeePvcStatus;
    @Mock public PodStatus jeePodStatus;
    @Mock public PodStatus dbPodStatus;

    public KubernetesPluginMocker() {
        setUp();
    }

    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(plugin.getSpec()).thenReturn(spec);
        when(plugin.getMetadata()).thenReturn(metadata);
        when(spec.getEntandoStatus()).thenReturn(entandoStatus);
        when(entandoStatus.getDbServerStatus()).thenReturn(singletonList(dbServerStatus));
        when(entandoStatus.getJeeServerStatus()).thenReturn(singletonList(jeeServerStatus));
        when(dbServerStatus.getDeploymentStatus()).thenReturn(dbDeploymentStatus);
        when(dbServerStatus.getPodStatus()).thenReturn(dbPodStatus);
        when(dbServerStatus.getPersistentVolumeClaimStatus()).thenReturn(dbPvcStatus);
        when(jeeServerStatus.getDeploymentStatus()).thenReturn(jeeDeploymentStatus);
        when(jeeServerStatus.getPersistentVolumeClaimStatus()).thenReturn(jeePvcStatus);
        when(jeeServerStatus.getPodStatus()).thenReturn(jeePodStatus);
    }

    public void setDeploymentPhase(final EntandoDeploymentPhase phase) {
        when(entandoStatus.getEntandoDeploymentPhase()).thenReturn(phase);
    }

    public void setPvcPhase(final String phase) {
        when(dbPvcStatus.getPhase()).thenReturn(phase);
        when(jeePvcStatus.getPhase()).thenReturn(phase);
    }

    public void setIngresPath(final String ingresPath) {
        when(spec.getIngressPath()).thenReturn(ingresPath);
    }

    public void setReplicas(final int replicas) {
        when(spec.getReplicas()).thenReturn(replicas);
        when(jeeDeploymentStatus.getReplicas()).thenReturn(replicas);
        when(jeeDeploymentStatus.getAvailableReplicas()).thenReturn(replicas);
        when(jeeDeploymentStatus.getReadyReplicas()).thenReturn(replicas);
        when(jeeDeploymentStatus.getUpdatedReplicas()).thenReturn(replicas);
    }

    public void setDbPodStatus(final String phase, final List<PodCondition> conditions) {
        when(dbPodStatus.getPhase()).thenReturn(phase);
        when(dbPodStatus.getConditions()).thenReturn(conditions);
    }

    public void setDbDeploymentStatus(final List<DeploymentCondition> conditions) {
        when(dbDeploymentStatus.getConditions()).thenReturn(conditions);
        when(dbDeploymentStatus.getReplicas()).thenReturn(1);
        when(dbDeploymentStatus.getAvailableReplicas()).thenReturn(1);
        when(dbDeploymentStatus.getReadyReplicas()).thenReturn(1);
        when(dbDeploymentStatus.getUpdatedReplicas()).thenReturn(1);
    }

    public void setJeePodStatus(final String phase, final List<PodCondition> conditions) {
        when(jeePodStatus.getPhase()).thenReturn(phase);
        when(jeePodStatus.getConditions()).thenReturn(conditions);
    }

    public void setJeeDeploymentStatus(final List<DeploymentCondition> conditions) {
        when(jeeDeploymentStatus.getConditions()).thenReturn(conditions);
    }

    public void setMetadataName(final String name) {
        when(metadata.getName()).thenReturn(name);
    }

}
