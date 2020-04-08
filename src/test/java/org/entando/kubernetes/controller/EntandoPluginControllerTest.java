package org.entando.kubernetes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.BASE_PLUGIN_ENDPOINT;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.getTestEntandoPlugin;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestJwtDecoderConfig;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.IngressService;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.HalUtils;
import org.entando.kubernetes.util.IngressTestHelper;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.UriComponentsBuilder;

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
@Tag("component")
public class EntandoPluginControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoPluginService entandoPluginService;

    @MockBean
    private IngressService ingressService;

    @Test
    public void shouldReturnEmptyListIfNotPluginIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoPluginService, times(1)).getAll();
    }

    @Test
    public void shouldReturnAListWithOnePlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .queryParam("namespace", TEST_PLUGIN_NAMESPACE)
                .build().toUri();


        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        when(entandoPluginService.getAllInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempPlugin));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoPlugins").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoPlugins[0].metadata.name" ).value(TEST_PLUGIN_NAME))
                .andExpect(jsonPath("$._embedded.entandoPlugins[0].metadata.namespace").value(TEST_PLUGIN_NAMESPACE));

        verify(entandoPluginService, times(1)).getAllInNamespace(TEST_PLUGIN_NAMESPACE);
    }

    @Test
    public void shouldReturnCollectionLinks() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .queryParam("namespace", TEST_PLUGIN_NAMESPACE)
                .build().toUri();


        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        when(entandoPluginService.getAllInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempPlugin));
        MvcResult result =mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        CollectionModel<EntityModel<EntandoPlugin>> appCollection =
                HalUtils.halMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<CollectionModel<EntityModel<EntandoPlugin>>>() {}
                );
        Links cl = appCollection.getLinks();
        assertThat(cl).isNotEmpty();
        assertThat(cl.stream().map(Link::getRel).map(LinkRelation::value).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("plugin", "plugins-in-namespace", "plugin-links");
        assertThat(cl.stream().allMatch(Link::isTemplated)).isTrue();
    }

    @Test
    public void shouldReturnPluginByName() throws Exception {
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        String pluginName = tempPlugin.getMetadata().getName();
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .pathSegment(pluginName)
                .build().toUri();

        when(entandoPluginService.findByName(eq(pluginName))).thenReturn(Optional.of(tempPlugin));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name").value(pluginName));

    }

    @Test
    public void shouldGetPluginIngress() throws Exception {
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        String pluginName = tempPlugin.getMetadata().getName();
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .pathSegment(pluginName, "ingress")
                .build().toUri();

        Ingress pluginIngress = IngressTestHelper.getIngressForEntandoResource(tempPlugin);

        when(entandoPluginService.findByName(eq(pluginName))).thenReturn(Optional.of(tempPlugin));
        when(ingressService.findByEntandoPlugin(any(EntandoPlugin.class))).thenReturn(Optional.of(pluginIngress));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name").value(pluginName + "-ingress"))
                .andExpect(jsonPath("$.metadata.labels.EntandoPlugin").value(pluginName));


    }

    @Test
    public void shouldReturn404IfIngressNotFound() throws Exception {
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        String pluginName = tempPlugin.getMetadata().getName();
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .pathSegment(pluginName, "ingress")
                .build().toUri();

        when(entandoPluginService.findByName(eq(pluginName))).thenReturn(Optional.of(tempPlugin));
        when(ingressService.findByEntandoPlugin(any(EntandoPlugin.class))).thenReturn(Optional.empty());

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringContains.containsString("Ingress not found for EntandoPlugin")));
    }

    @Test
    public void shouldReturn404IfPluginNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .pathSegment(TEST_PLUGIN_NAMESPACE, TEST_PLUGIN_NAME)
                .build().toUri();
        mvc.perform(get(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldThrowBadRequestExceptionForAlreadyDeployedPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .build().toUri();
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();

        when(entandoPluginService.findByName(eq(TEST_PLUGIN_NAME)))
                .thenReturn(Optional.of(tempPlugin));

        mvc.perform(post(uri)
                .content(mapper.writeValueAsString(tempPlugin))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldReturnCreatedForNewlyDeployedPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .build().toUri();

        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();

        when(entandoPluginService.deploy(any(EntandoPlugin.class))).thenReturn(tempPlugin);

        mvc.perform(post(uri)
                .content(mapper.writeValueAsString(tempPlugin))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.self.href").value(endsWith(Paths.get("plugins", TEST_PLUGIN_NAME).toString())))
                .andExpect(jsonPath("$._links.plugins").exists());

    }

    @Test
    public void shouldReturnAcceptedWhenDeletingAPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_PLUGIN_ENDPOINT)
                .pathSegment(TEST_PLUGIN_NAME)
                .build().toUri();
        when(entandoPluginService.findByName(eq(TEST_PLUGIN_NAME)))
                .thenReturn(Optional.of(getTestEntandoPlugin()));

        mvc.perform(delete(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

    }
}
