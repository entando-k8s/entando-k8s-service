package org.entando.kubernetes;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.api.model.apiextensions.DoneableCustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.*;
import org.entando.kubernetes.model.plugin.DoneableEntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginList;
import org.entando.kubernetes.service.EntandoPluginService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class KubernetesClientMocker {

    private final KubernetesClient client;

    @Mock public NonNamespaceOperation<CustomResourceDefinition, CustomResourceDefinitionList, DoneableCustomResourceDefinition,
            Resource<CustomResourceDefinition, DoneableCustomResourceDefinition>> resourceOperation;
    @Mock public MixedOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin,
            Resource<EntandoPlugin, DoneableEntandoPlugin>> mixedOperation;
    @Mock public FilterWatchListMultiDeletable<EntandoPlugin, EntandoPluginList, Boolean, Watch, Watcher<EntandoPlugin>> anyNamespaceOperations;
    @Mock public NonNamespaceOperation<EntandoPlugin, EntandoPluginList, DoneableEntandoPlugin, Resource<EntandoPlugin, DoneableEntandoPlugin>> namespaceOperations;
    @Mock public CustomResourceDefinition customResourceDefinition;
    @Mock public Resource<CustomResourceDefinition, DoneableCustomResourceDefinition> resource;
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
        when(resourceOperation.withName(EntandoPlugin.CRD_NAME)).thenReturn(resource);
        when(resource.get()).thenReturn(customResourceDefinition);
        when(client.customResources(same(customResourceDefinition), same(EntandoPlugin.class),
                same(EntandoPluginList.class), same(DoneableEntandoPlugin.class)))
                .thenReturn(mixedOperation);
        when(mixedOperation.inAnyNamespace()).thenReturn(anyNamespaceOperations);
        when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperations);
        when(anyNamespaceOperations.list()).thenReturn(pluginList);
        when(namespaceOperations.list()).thenReturn(pluginList);
    }

    public void mockResult(final String pluginId, final EntandoPlugin plugin) {
        @SuppressWarnings("unchecked")
        final Resource<EntandoPlugin, DoneableEntandoPlugin> pluginResource = Mockito.mock(Resource.class);
        pluginList.setItems(Collections.singletonList(plugin));
        when(pluginResource.get()).thenReturn(plugin);
    }



}
