package org.entando.kubernetes.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.entando.kubernetes.KubernetesClientMocker;
import org.entando.kubernetes.model.*;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.plugin.EntandoPluginSpec;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.Collections;

import static com.jayway.jsonpath.JsonPath.using;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.kubernetes.KubernetesHelpers.createDeploymentCondition;
import static org.entando.kubernetes.KubernetesHelpers.createPodCondition;
import static org.entando.kubernetes.TestHelpers.extractFromJson;
import static org.entando.kubernetes.model.plugin.EntandoPluginSpec.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public class PluginControllerTest {

    private static final String URL = "/plugins";

    @Autowired private MockMvc mockMvc;
    @Autowired private KubernetesClient client;

    private KubernetesClientMocker mocker;

    @Before
    public void setUp() {
        mocker = new KubernetesClientMocker(client);
    }

    @Test
    public void testListEmpty() throws Exception {
        when(mocker.pluginList.getItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    public void testNotFound() throws Exception {
        final String pluginId = "arbitrary-plugin";
        mocker.mockResult(pluginId, null);

        mockMvc.perform(get(String.format("%s/%s", URL, pluginId)))
                .andDo(print()).andExpect(status().isNotFound());

        verify(mocker.mixedOperation, times(1)).inAnyNamespace();
    }

    @Test
    public void testList() throws Exception {
        EntandoPlugin plugin = new EntandoPlugin();

        ObjectMeta pluginMeta = new ObjectMetaBuilder().withName("plugin-name").withNamespace("plugin-namespace").build();

        EntandoPluginSpec pluginSpec = new EntandoPluginSpecBuilder()
                .withEntandoApp("entando-app-namespace", "entando-app")
                .withIngressPath("/pluginpath")
                .withReplicas(1)
                .withKeycloakServer("keycloak-namespace", "keycloak-server")
                .withDbms(DbmsImageVendor.MYSQL)
                .withHealthCheckPath("/pluginpath/health")
                .withImage("pluginimage")
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

        when(mocker.pluginList.getItems()).thenReturn(singletonList(plugin));

        MvcResult result =  mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.plugins", hasSize(1)))
                .andReturn();

        Resource<EntandoPlugin> pluginResource = extractFromJson(result.getResponse().getContentAsString(), "$._embedded.plugins[0]", new TypeReference<Resource<EntandoPlugin>>() {});

        assertThat(pluginResource.getContent()).isEqualToComparingFieldByFieldRecursively(plugin);
        assertThat(pluginResource.hasLink("self"));
    }

}
