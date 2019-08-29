package org.entando.kubernetes.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.entando.kubernetes.KubernetesClientMocker;
import org.entando.kubernetes.KubernetesPluginMocker;
import org.entando.kubernetes.model.plugin.EntandoDeploymentPhase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("payload", hasSize(0)));
    }

    @Test
    public void testNotFound() throws Exception {
        final String pluginId = "arbitrary-plugin";
        mocker.mockResult(pluginId, null);

        mockMvc.perform(get(String.format("%s/%s", URL, pluginId)))
                .andDo(print()).andExpect(status().isNotFound());

        verify(mocker.operation, times(1)).withName(eq(pluginId));
    }

    @Test
    public void testList() throws Exception {
        final KubernetesPluginMocker pluginMocker = new KubernetesPluginMocker();

        pluginMocker.setDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL);
        pluginMocker.setIngresPath("/pluginpath");
        pluginMocker.setReplicas(1);

        pluginMocker.setDbPodStatus("Running", asList(
            mocker.mockPodCondition("2019-07-11T18:36:09Z", "Available"),
            mocker.mockPodCondition("2019-07-11T18:36:06Z", "Initialized")
        ));
        pluginMocker.setDbDeploymentStatus(asList(
                mocker.mockDeploymentCondition("2019-07-11T18:36:06Z", "Some message",
                        "MinimumReplicasAvailable", "Available"),
                mocker.mockDeploymentCondition("2019-07-11T18:36:03Z", "Some message",
                        "NewReplicaSetAvailable", "Progressing")
        ));
        pluginMocker.setPvcPhase("Bound");

        pluginMocker.setJeePodStatus("Running", singletonList(
                mocker.mockPodCondition("2019-07-11T18:36:06Z", "Initialized")));
        pluginMocker.setJeeDeploymentStatus(singletonList(
                mocker.mockDeploymentCondition("2019-07-11T18:36:06Z", "Some message",
                        "NewReplicaSetAvailable", "Progressing")));

        pluginMocker.setMetadataName("plugin-name");
        when(mocker.pluginList.getItems()).thenReturn(singletonList(pluginMocker.plugin));

        ResultActions resultActions = mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("payload", hasSize(1)));
        validate(resultActions, "payload[0].");

        mocker.mockResult("plugin-name", pluginMocker.plugin);
        resultActions = mockMvc.perform(get(String.format("%s/plugin-name", URL)))
                .andDo(print()).andExpect(status().isOk());
        validate(resultActions, "payload.");
    }

    private void validate(final ResultActions actions, final String prefix) throws Exception {
        actions.andExpect(jsonPath(prefix + "plugin").value("plugin-name"))
                .andExpect(jsonPath(prefix + "online").value(true))
                .andExpect(jsonPath(prefix + "path").value("/pluginpath"))
                .andExpect(jsonPath(prefix + "replicas").value(1))
                .andExpect(jsonPath(prefix + "deploymentPhase").value("successful"))
                .andExpect(jsonPath(prefix + "serverStatus.type").value("jeeServer"))
                .andExpect(jsonPath(prefix + "serverStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.volumePhase").value("Bound"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.phase").value("Running"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions", hasSize(1)))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "serverStatus.podStatus.conditions[0].type").value("Initialized"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.availableReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.readyReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.updatedReplicas").value(1))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].lastUpdateTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].type").value("Progressing"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].reason").value("NewReplicaSetAvailable"))
                .andExpect(jsonPath(prefix + "serverStatus.deploymentStatus.conditions[0].message").value("Some message"))

                .andExpect(jsonPath(prefix + "externalServiceStatuses", hasSize(1)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].type").value("dbServer"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].replicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].volumePhase").value("Bound"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.phase").value("Running"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions", hasSize(2)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[0].type").value("Initialized"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].lastTransitionTime").value("2019-07-11T18:36:09Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].podStatus.conditions[1].type").value("Available"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.availableReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.readyReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.replicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.updatedReplicas").value(1))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions", hasSize(2)))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].lastTransitionTime").value("2019-07-11T18:36:03Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].lastUpdateTime").value("2019-07-11T18:36:03Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].type").value("Progressing"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].reason").value("NewReplicaSetAvailable"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[0].message").value("Some message"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].lastTransitionTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].lastUpdateTime").value("2019-07-11T18:36:06Z"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].status").value("True"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].type").value("Available"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].reason").value("MinimumReplicasAvailable"))
                .andExpect(jsonPath(prefix + "externalServiceStatuses[0].deploymentStatus.conditions[1].message").value("Some message"))
        ;
    }

}
