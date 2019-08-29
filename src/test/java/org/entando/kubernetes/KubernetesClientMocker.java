package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.service.KubernetesService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class KubernetesClientMocker {

    private final KubernetesClient client;

    @Mock public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, DoneableCustomResourceDefinition,
            Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>> resourceOperation;
    @Mock public NonNamespaceOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin,
            Resource<EntandoPlugin, DoneableEntandoPlugin>> operation;
    @Mock public CustomResourceDefinition customResourceDefinition;
    @Mock public Resource<CustomResourceDefinition, DoneableCustomResourceDefinition> resource;
    @Mock public MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin,
            Resource<EntandoPlugin, DoneableEntandoPlugin>> mixedOperation;
    @Mock public EntandoPluginList pluginList;

    public KubernetesClientMocker(final KubernetesClient client) {
        this.client = client;
        setUp();
    }

    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reset(client);
        defineMocks();
    }

    private void defineMocks() {
        when(client.customResourceDefinitions()).thenReturn(resourceOperation);
        when(resourceOperation.withName(KubernetesService.ENTANDOPLUGIN_CRD_NAME)).thenReturn(resource);
        when(resource.get()).thenReturn(customResourceDefinition);
        when(client.customResources(same(customResourceDefinition), same(EntandoPlugin.class),
                same(EntandoPluginList.class), same(DoneableEntandoPlugin.class)))
                .thenReturn(mixedOperation);
        when(mixedOperation.inNamespace(anyString())).thenReturn(operation);
        when(operation.list()).thenReturn(pluginList);
    }

    public void mockResult(final String pluginId, final EntandoPlugin plugin) {
        @SuppressWarnings("unchecked")
        final Resource<EntandoPlugin, DoneableEntandoPlugin> pluginResource = Mockito.mock(Resource.class);
        when(operation.withName(eq(pluginId))).thenReturn(pluginResource);
        when(pluginResource.get()).thenReturn(plugin);
    }

    public DeploymentCondition mockDeploymentCondition(final String ts, final String message,
                                                       final String reason, final String type) {
        final DeploymentCondition condition = new DeploymentCondition();
        condition.setLastTransitionTime(ts);
        condition.setLastUpdateTime(ts);
        condition.setMessage(message);
        condition.setReason(reason);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }

    public PodCondition mockPodCondition(final String ts, final String type) {
        final PodCondition condition = new PodCondition();
        condition.setLastTransitionTime(ts);
        condition.setStatus("True");
        condition.setType(type);
        return condition;
    }

}
