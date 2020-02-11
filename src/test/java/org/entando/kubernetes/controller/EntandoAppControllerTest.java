package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
public class EntandoAppControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoAppService entandoAppService;

    @MockBean
    private EntandoLinkService entandoLinkService;

    @MockBean
    private EntandoPluginService entandoPluginService;

    @Test
    public void shouldReturnEmptyListIfNotAppIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoAppService, times(1)).getApps();
    }

    @Test
    public void shouldReturnAListWithOneApp() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE)
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp();
        when(entandoAppService.getAppsInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempApp));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoAppList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoAppList[0].metadata.name" ).value(TEST_APP_NAME))
                .andExpect(jsonPath("$._embedded.entandoAppList[0].metadata.namespace").value(TEST_APP_NAMESPACE));


        verify(entandoAppService, times(1)).getAppsInNamespace(TEST_APP_NAMESPACE);
    }

    @Test
    public void shouldReturn404IfAppNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME)
                .build().toUri();
        mvc.perform(get(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldReturn404WhenGettingLinksIfAppNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();

        mvc.perform(get(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    public void shouldReturnAppLinks() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();

        when(entandoAppService.findAppByNameAndNamespace(anyString(), anyString())).thenReturn(Optional.of(ea));
        when(entandoLinkService.listAppLinks(any(EntandoApp.class))).thenReturn(Collections.singletonList(el));

        final String linkEntryJsonPath = "$._embedded.entandoAppPluginLinkList[0]";
        final String linkHateoasLinksJsonPath = linkEntryJsonPath + "._links";

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(linkEntryJsonPath + ".metadata.name").value(el.getMetadata().getName()))
                .andExpect(jsonPath(linkEntryJsonPath + ".spec").value(allOf(
                        hasEntry("entandoAppNamespace", TEST_APP_NAMESPACE),
                        hasEntry("entandoPluginNamespace", TEST_PLUGIN_NAMESPACE),
                        hasEntry("entandoPluginName", TEST_PLUGIN_NAME),
                        hasEntry("entandoAppName", TEST_APP_NAME)
                )))
                .andExpect(jsonPath(linkHateoasLinksJsonPath).exists())
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".app.href").value(endsWith("my-app-namespace/my-app")))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".plugin.href").value(endsWith("plugins/my-plugin")));

    }

    @Test
    public void shouldCreateLinkBetweenExistingAppAndPlugin() throws Exception {
        when(entandoLinkService.generateForAppAndPlugin(any(EntandoApp.class), any(EntandoPlugin.class))).thenCallRealMethod();

        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        EntandoAppPluginLink el = entandoLinkService.generateForAppAndPlugin(ea, ep);

        when(entandoAppService.findAppByNameAndNamespace(anyString(), anyString())).thenReturn(Optional.of(ea));
        when(entandoPluginService.findPluginById(eq(ep.getMetadata().getName()))).thenReturn(Optional.of(ep));
        when(entandoLinkService.deploy(any(EntandoAppPluginLink.class))).thenReturn(el);


        mvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath( "$.metadata.name").value(el.getMetadata().getName()))
                .andExpect(jsonPath("$.spec").value(allOf(
                        hasEntry("entandoAppNamespace", TEST_APP_NAMESPACE),
                        hasEntry("entandoPluginNamespace", TEST_PLUGIN_NAMESPACE),
                        hasEntry("entandoPluginName", TEST_PLUGIN_NAME),
                        hasEntry("entandoAppName", TEST_APP_NAME)
                )))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath( "$._links.app.href").value(endsWith("my-app-namespace/my-app")))
                .andExpect(jsonPath( "$._links.plugin.href").value(endsWith("plugins/my-plugin")));

    }


    @Test
    public void shouldDeployPluginIfNoneIsFoundWhileCreatingLink() throws Exception {
        when(entandoLinkService.generateForAppAndPlugin(any(EntandoApp.class), any(EntandoPlugin.class))).thenCallRealMethod();

        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();

        EntandoAppPluginLink el = entandoLinkService.generateForAppAndPlugin(ea, ep);

        ArgumentCaptor<EntandoPlugin> argumentCaptor = ArgumentCaptor.forClass(EntandoPlugin.class);
        when(entandoAppService.findAppByNameAndNamespace(anyString(), anyString())).thenReturn(Optional.of(ea));
        when(entandoLinkService.deploy(any(EntandoAppPluginLink.class))).thenReturn(el);
        when(entandoPluginService.deploy(any(EntandoPlugin.class))).thenReturn(ep);


        mvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isCreated());

        verify(entandoPluginService, times(1)).deploy(argumentCaptor.capture());
        EntandoPlugin passedPlugin = argumentCaptor.getValue();
        assertEquals(ep.getMetadata().getNamespace(), passedPlugin.getMetadata().getNamespace());
    }

    @Test
    public void shouldDeployPluginOnFallbackNamespaceIfNoneIsFoundWhileCreatingLink() throws Exception {
        when(entandoLinkService.generateForAppAndPlugin(any(EntandoApp.class), any(EntandoPlugin.class))).thenCallRealMethod();

        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        ep.getMetadata().setNamespace(null);

        EntandoAppPluginLink el = entandoLinkService.generateForAppAndPlugin(ea, ep);

        ArgumentCaptor<EntandoPlugin> argumentCaptor = ArgumentCaptor.forClass(EntandoPlugin.class);
        when(entandoAppService.findAppByNameAndNamespace(anyString(), anyString())).thenReturn(Optional.of(ea));
        when(entandoLinkService.deploy(any(EntandoAppPluginLink.class))).thenReturn(el);
        when(entandoPluginService.deploy(any(EntandoPlugin.class))).thenReturn(ep);


        mvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isCreated());

        verify(entandoPluginService, times(1)).deploy(argumentCaptor.capture());
        EntandoPlugin passedPlugin = argumentCaptor.getValue();
        assertEquals(ea.getMetadata().getNamespace(), passedPlugin.getMetadata().getNamespace());
    }

    @Test
    public void shouldDeleteExistingLinkBetweenAppAndPlugin() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links", TEST_PLUGIN_NAME)
                .build().toUri();

        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();

        when(entandoLinkService.listEntandoAppLinks(TEST_APP_NAMESPACE, TEST_APP_NAME))
                .thenReturn(Collections.singletonList(el));

        mvc.perform(delete(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(entandoLinkService, times(1)).delete(any(EntandoAppPluginLink.class));
    }

    @Test
    public void shouldThrowAnErrorWhenCreatingLinkButAppDoesntExist() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_APP_NAMESPACE, TEST_APP_NAME, "links")
                .build().toUri();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        mvc.perform(post(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(ep)))
                .andExpect(status().isNotFound());

        verify(entandoAppService, times(1)).findAppByNameAndNamespace(TEST_APP_NAME, TEST_APP_NAMESPACE);

    }

}
