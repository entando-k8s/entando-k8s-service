package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestJwtDecoderConfig;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {
                EntandoKubernetesJavaApplication.class,
                TestSecurityConfiguration.class,
                TestKubernetesConfig.class,
                TestJwtDecoderConfig.class
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EntandoPluginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoPluginService entandoPluginService;

    @Test
    public void shouldReturnEmptyListIfNotPluginIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoPluginService, times(1)).getPlugins();
    }

    @Test
    public void shouldReturnAListWithOnePlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .queryParam("namespace", TEST_PLUGIN_NAMESPACE)
                .build().toUri();


        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        when(entandoPluginService.getPluginsInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempPlugin));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoPluginList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoPluginList[0].metadata.name" ).value(TEST_PLUGIN_NAME))
                .andExpect(jsonPath("$._embedded.entandoPluginList[0].metadata.namespace").value(TEST_PLUGIN_NAMESPACE));

        verify(entandoPluginService, times(1)).getPluginsInNamespace(TEST_PLUGIN_NAMESPACE);
    }

    @Test
    public void shouldReturn404IfPluginNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .pathSegment(TEST_PLUGIN_NAME)
                .build().toUri();
        mvc.perform(get(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldThrowBadRequestExceptionForAlreadyDeployedPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .build().toUri();
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();

        when(entandoPluginService.findPluginByIdAndNamespace(eq(TEST_PLUGIN_NAME), any()))
                .thenReturn(Optional.of(tempPlugin));

        mvc.perform(post(uri)
                .content(mapper.writeValueAsString(tempPlugin))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void shouldReturnCreatedForNewlyDeployedPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .build().toUri();

        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        tempPlugin.getMetadata().setNamespace("my-namespace");

        when(entandoPluginService.deploy(any(EntandoPlugin.class))).thenReturn(tempPlugin);

        mvc.perform(post(uri)
                .content(mapper.writeValueAsString(tempPlugin))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.self.href").value(endsWith("plugins/"+TEST_PLUGIN_NAME)))
                .andExpect(jsonPath("$._links.plugins").exists());

    }

    @Test
    public void shouldReturnAcceptedWhenDeletingAPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT)
                .pathSegment(TEST_PLUGIN_NAME)
                .build().toUri();

        mvc.perform(delete(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

    }
}