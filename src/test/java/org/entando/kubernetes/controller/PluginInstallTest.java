package org.entando.kubernetes.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.entando.kubernetes.KubernetesClientMocker;
import org.entando.kubernetes.model.DbmsImageVendor;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoPluginService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.KubernetesHelpers.getTestEntandoPlugin;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public class PluginInstallTest {

    @Autowired private EntandoPluginService entandoPluginService;
    @Autowired private KubernetesClient client;

    private KubernetesClientMocker mocker;

    @Before
    public void setUp() {
        mocker = new KubernetesClientMocker(client);
    }

    @Test
    public void testDeployment() {

        EntandoPlugin entandoPlugin = getTestEntandoPlugin();

        entandoPluginService.deploy(entandoPlugin);

        final ArgumentCaptor<EntandoPlugin> captor = ArgumentCaptor.forClass(EntandoPlugin.class);
        verify(mocker.mixedOperation.inNamespace("plugin-namespace"), times(1)).create(captor.capture());
        final EntandoPlugin plugin = captor.getValue();

        assertThat(plugin.getSpec().getIngressPath()).isEqualTo("/pluginpath");
        assertThat(plugin.getSpec().getDbms()).isEqualTo(Optional.of(DbmsImageVendor.MYSQL));
        assertThat(plugin.getSpec().getImage()).isEqualTo("entando/entando-plugin-image");
        assertThat(plugin.getSpec().getHealthCheckPath()).isEqualTo("/actuator/health");
        assertThat(plugin.getSpec().getReplicas()).isEqualTo(1);
        assertThat(plugin.getMetadata().getName()).isEqualTo("plugin-name");

        assertThat(plugin.getSpec().getRoles()).hasSize(1);
        assertThat(plugin.getSpec().getRoles().get(0).getCode()).isEqualTo("read");
        assertThat(plugin.getSpec().getRoles().get(0).getName()).isEqualTo("Read");

        assertThat(plugin.getSpec().getPermissions()).hasSize(1);
        assertThat(plugin.getSpec().getPermissions().get(0).getClientId()).isEqualTo("another-client");
        assertThat(plugin.getSpec().getPermissions().get(0).getRole()).isEqualTo("read");
    }

}
