package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.networking.v1.Ingress;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoCustomResourceStatus;
import org.entando.kubernetes.model.common.EntandoDeploymentPhase;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.link.EntandoAppPluginLinkBuilder;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.service.IngressService;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.IngressTestHelper;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {
                EntandoKubernetesJavaApplication.class,
                TestKubernetesConfig.class
        })
@ActiveProfiles("test")
@Tag("component")
@WithMockUser
class EntandoAppControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoAppService entandoAppService;

    @MockBean
    private EntandoLinkService entandoLinkService;

    @MockBean
    private EntandoPluginService entandoPluginService;

    @MockBean
    private IngressService ingressService;

    public static final String UNDEFINED = "undefined";

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturnEmptyListIfNotAppIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("[]"));

        verify(entandoAppService, times(1)).getAll();
    }

    @Test
    void shouldReturnAListWithOneApp() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .queryParam("namespace", TEST_APP_NAMESPACE)
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp();
        when(entandoAppService.getAllInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempApp));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.[0].metadata.name").value(TEST_APP_NAME))
                .andExpect(jsonPath("$.[0].metadata.namespace").value(TEST_APP_NAMESPACE));

        verify(entandoAppService, times(1)).getAllInNamespace(TEST_APP_NAMESPACE);
    }

    @Test
    void shouldReturnCollectionLinks() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .queryParam("namespace", TEST_APP_NAMESPACE)
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp();
        when(entandoAppService.getAllInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempApp));
        MvcResult result = mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        /*
        CollectionModel<EntityModel<EntandoApp>> appCollection =
                HalUtils.halMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<CollectionModel<EntityModel<EntandoApp>>>() {
                        }
                );
        Links cl = appCollection.getLinks();
        assertThat(cl).isNotEmpty();
        assertThat(cl.stream().map(Link::getRel).map(LinkRelation::value).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("app", "apps-in-namespace", "app-links");
        assertThat(cl.stream().allMatch(Link::isTemplated)).isTrue();
        */
    }

    @Test
    void shouldReturn404IfAppNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME)
                .build().toUri();
        mvc.perform(get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldReturnEntandoAppStatus() throws Exception {

        final URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "status")
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp();
        EntandoCustomResourceStatus status = new EntandoCustomResourceStatus();
        status.updateDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL, 12L);
        tempApp.setStatus(status);

        when(entandoAppService.findByNameAndDefaultNamespace(TEST_APP_NAME)).thenReturn(Optional.ofNullable(tempApp));

        MvcResult result = mvc.perform(get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EntandoDeploymentPhase.SUCCESSFUL.toValue()))
                .andReturn();

    }

    @Test
    void shouldReturn404_or_Unknow_EntandoAppStatus() throws Exception {

        final URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "status")
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp();
        EntandoCustomResourceStatus status = new EntandoCustomResourceStatus();
        status.updateDeploymentPhase(null, 12L);
        tempApp.setStatus(status);

        when(entandoAppService.findByNameAndDefaultNamespace(TEST_APP_NAME)).thenReturn(Optional.ofNullable(tempApp));

        MvcResult result = mvc.perform(get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(UNDEFINED))
                .andReturn();

        when(entandoAppService.findByNameAndDefaultNamespace(TEST_APP_NAME)).thenReturn(Optional.empty());
        mvc.perform(get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

    }


    @Test
    void shouldGetPluginIngress() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "ingress")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        String entandoAppName = ea.getMetadata().getName();

        Ingress appIngress = IngressTestHelper.getIngressForEntandoResource(ea);

        when(entandoAppService.findByNameAndDefaultNamespace(entandoAppName)).thenReturn(Optional.of(ea));
        when(ingressService.findByEntandoApp(any(EntandoApp.class))).thenReturn(Optional.of(appIngress));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name").value(entandoAppName + "-ingress"))
                .andExpect(jsonPath("$.metadata.labels.EntandoApp").value(entandoAppName));
    }

    @Test
    void shouldReturn404IfIngressNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "ingress")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        String entandoAppName = ea.getMetadata().getName();

        when(entandoAppService.findByNameAndDefaultNamespace(entandoAppName)).thenReturn(Optional.of(ea));
        when(ingressService.findByEntandoApp(any(EntandoApp.class))).thenReturn(Optional.empty());

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(StringContains.containsString("Ingress not found for EntandoApp")));
    }

    @Test
    void shouldReturn404WhenGettingLinksIfAppNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();

        mvc.perform(get(uri)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    void shouldCreateLinkBetweenExistingAppAndPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        EntandoAppPluginLink el = stubEntandoAppPluginLink();

        when(entandoAppService.findByNameAndDefaultNamespace(anyString())).thenReturn(Optional.of(ea));
        when(entandoPluginService.deploy(any(EntandoPlugin.class), eq(true))).thenReturn(ep);
        when(entandoLinkService.deploy(any(EntandoAppPluginLink.class))).thenReturn(el);
        when(entandoLinkService.buildBetweenAppAndPlugin(any(EntandoApp.class), any(EntandoPlugin.class))).thenReturn(
                el);

        mvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metadata.name").value(el.getMetadata().getName()))
                .andExpect(jsonPath("$.spec").value(allOf(
                        hasEntry("entandoAppNamespace", TEST_APP_NAMESPACE),
                        hasEntry("entandoPluginNamespace", TEST_PLUGIN_NAMESPACE),
                        hasEntry("entandoPluginName", TEST_PLUGIN_NAME),
                        hasEntry("entandoAppName", TEST_APP_NAME)
                )));

        // ensure entandoPluginService is asked to deploy the plugin
        ArgumentCaptor<EntandoPlugin> argCapt = ArgumentCaptor.forClass(EntandoPlugin.class);
        verify(entandoPluginService, times(1)).deploy(argCapt.capture(), eq(true));
    }

    @Test
    void shouldDeployPluginOnFallbackNamespaceIfNoneIsFoundWhileCreatingLink() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        ep.getMetadata().setNamespace(null);

        EntandoAppPluginLink el = stubEntandoAppPluginLink();

        ArgumentCaptor<EntandoPlugin> argumentCaptor = ArgumentCaptor.forClass(EntandoPlugin.class);
        when(entandoAppService.findByNameAndDefaultNamespace(anyString())).thenReturn(Optional.of(ea));
        when(entandoLinkService.deploy(any(EntandoAppPluginLink.class))).thenReturn(el);
        when(entandoLinkService.buildBetweenAppAndPlugin(any(EntandoApp.class), any(EntandoPlugin.class))).thenReturn(
                el);
        when(entandoPluginService.deploy(any(EntandoPlugin.class), eq(true))).thenReturn(ep);

        mvc.perform(post(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isCreated());

        verify(entandoPluginService, times(1)).deploy(argumentCaptor.capture(), eq(true));
        EntandoPlugin passedPlugin = argumentCaptor.getValue();
        assertEquals(ea.getMetadata().getNamespace(), passedPlugin.getMetadata().getNamespace());
    }

    @Test
    void shouldThrowAnErrorWhenCreatingLinkButAppDoesntExist() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAME, "links")
                .build().toUri();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        mvc.perform(post(uri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isNotFound());

        verify(entandoAppService, times(1)).findByNameAndDefaultNamespace(TEST_APP_NAME);
        verify(entandoPluginService, times(0)).deletePlugin(any(EntandoPlugin.class));

    }

    private EntandoAppPluginLink stubEntandoAppPluginLink() {
        return new EntandoAppPluginLinkBuilder()
                .withNewSpec()
                .withEntandoApp(TEST_APP_NAMESPACE, TEST_APP_NAME)
                .withEntandoPlugin(TEST_PLUGIN_NAMESPACE, TEST_PLUGIN_NAME)
                .and()
                .withNewMetadata().withName("my-app-my-plugin-link").and().build();
    }

}
