package org.entando.kubernetes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAME;
import static org.entando.kubernetes.util.EntandoAppTestHelper.TEST_APP_NAMESPACE;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAME;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.model.common.EntandoDeploymentPhase;
import org.entando.kubernetes.model.link.EntandoAppPluginLink;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.model.request.AppPluginLinkRequest;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.service.EntandoLinkService;
import org.entando.kubernetes.service.EntandoPluginService;
import org.entando.kubernetes.util.EntandoAppTestHelper;
import org.entando.kubernetes.util.EntandoLinkTestHelper;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.entando.kubernetes.util.HalUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {
                EntandoKubernetesJavaApplication.class,
                TestKubernetesConfig.class
        })
@ActiveProfiles("test")
@Tag("component")
@WithMockUser
class EntandoLinksControllerTest {

    private MockMvc mvc;

    @MockBean
    private EntandoLinkService entandoLinkService;

    @MockBean
    private EntandoAppService entandoAppService;

    @MockBean
    private EntandoPluginService entandoPluginService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturnListOfLinks() throws Exception {
        when(entandoLinkService.getAll()).thenReturn(Collections.singletonList(EntandoLinkTestHelper.getTestLink()));

        MvcResult result = mvc.perform(get("/app-plugin-links").accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entandoAppPluginLinks.length()").value(1))
                .andReturn();
        CollectionModel<EntityModel<EntandoAppPluginLink>> linkCollection =
                HalUtils.halMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<CollectionModel<EntityModel<EntandoAppPluginLink>>>() {
                        }
                );
        Links cl = linkCollection.getLinks();
        assertThat(cl).isNotEmpty();
        assertThat(cl.stream().map(Link::getRel).map(LinkRelation::value).collect(Collectors.toList()))
                .containsExactlyInAnyOrder(
                        "app-plugin-link",
                        "app-links",
                        "delete-and-scale-down",
                        "plugin-links",
                        "delete",
                        "app-plugin-links-in-namespace");
        assertThat(cl.stream().allMatch(Link::isTemplated)).isTrue();
    }

    @Test
    void shouldReturnLinksFromNamespace() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        when(entandoLinkService.getAllInNamespace(el.getMetadata().getNamespace())).thenReturn(Collections.singletonList(el));

        mvc.perform(get("/app-plugin-links?namespace={namespace}", el.getMetadata().getNamespace())
                .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entandoAppPluginLinks.length()").value(1));
    }

    @Test
    void shouldReturnLinkByName() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        String name = el.getMetadata().getName();
        when(entandoLinkService.findByName(name)).thenReturn(Optional.of(el));

        MvcResult result = mvc.perform(get("/app-plugin-links/{name}", name)
                .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.name").value(name))
                .andReturn();
        EntityModel<EntandoAppPluginLink> link =
                HalUtils.halMapper().readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<EntityModel<EntandoAppPluginLink>>() {
                        }
                );
        Links cl = link.getLinks();
        assertThat(cl).isNotEmpty();
        assertThat(cl.stream().map(Link::getRel).map(LinkRelation::value).collect(Collectors.toList()))
                .containsExactlyInAnyOrder("self", "app", "plugin", "delete", "namespace");
    }

    @Test
    void shouldReturnLinksByPluginName() throws Exception {
        EntandoPlugin tempPlugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        EntandoAppPluginLink tempLink = EntandoLinkTestHelper.getTestLink();
        String linkName = tempLink.getMetadata().getName();
        String linkNamespace = tempLink.getMetadata().getNamespace();

        String pluginName = tempPlugin.getMetadata().getName();
        when(entandoLinkService.findByPluginName(eq(pluginName))).thenReturn(Collections.singletonList(tempLink));

        String linkEntryJsonPath = "$._embedded.entandoAppPluginLinks[0]";
        String linkHateoasLinksJsonPath = linkEntryJsonPath + "._links";

        mvc.perform(get("/app-plugin-links?plugin={name}", pluginName).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath(linkEntryJsonPath + ".spec.entandoPluginName").value(TEST_PLUGIN_NAME))
                .andExpect(jsonPath(linkEntryJsonPath + ".spec.entandoPluginNamespace").value(TEST_PLUGIN_NAMESPACE))
                .andExpect(jsonPath(linkHateoasLinksJsonPath).exists())
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".app.href").value(endsWith("apps/my-app")))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".plugin.href").value(
                        endsWith("plugins/" + TEST_PLUGIN_NAME + "?namespace=" + TEST_PLUGIN_NAMESPACE)))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".delete.href").value(endsWith("app-plugin-links/" + linkName)))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".namespace.href").value(endsWith("namespaces/" + linkNamespace)));

    }

    @Test
    void shouldReturnLinksByAppName() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        String appName = ea.getMetadata().getName();
        String linkNamespace = el.getMetadata().getNamespace();

        when(entandoLinkService.findByAppName(eq(appName))).thenReturn(Collections.singletonList(el));

        String linkEntryJsonPath = "$._embedded.entandoAppPluginLinks[0]";
        String linkHateoasLinksJsonPath = linkEntryJsonPath + "._links";

        mvc.perform(get("/app-plugin-links?app={name}", appName).accept(HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(linkEntryJsonPath + ".metadata.name").value(el.getMetadata().getName()))
                .andExpect(jsonPath(linkEntryJsonPath + ".spec").value(allOf(
                        hasEntry("entandoAppNamespace", TEST_APP_NAMESPACE),
                        hasEntry("entandoPluginNamespace", TEST_PLUGIN_NAMESPACE),
                        hasEntry("entandoPluginName", TEST_PLUGIN_NAME),
                        hasEntry("entandoAppName", TEST_APP_NAME)
                )))
                .andExpect(jsonPath(linkHateoasLinksJsonPath).exists())
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".app.href").value(endsWith("apps/my-app")))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".plugin.href").value(
                        endsWith("plugins/" + TEST_PLUGIN_NAME + "?namespace=" + TEST_PLUGIN_NAMESPACE)))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".delete.href")
                        .value(endsWith("app-plugin-links/" + el.getMetadata().getName())))
                .andExpect(jsonPath(linkHateoasLinksJsonPath + ".namespace.href").value(endsWith("namespaces/" + linkNamespace)));
    }

    @Test
    void shouldCreatedLinkBetweenAppAndPlugin() throws Exception {
        EntandoApp ea = EntandoAppTestHelper.getTestEntandoApp();
        EntandoPlugin ep = EntandoPluginTestHelper.getTestEntandoPlugin();
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();

        when(entandoAppService.findByName(eq(ea.getMetadata().getName()))).thenReturn(Optional.of(ea));
        when(entandoPluginService.findByName(eq(ep.getMetadata().getName()))).thenReturn(Optional.of(ep));
        when(entandoLinkService.buildBetweenAppAndPlugin(eq(ea), eq(ep))).thenReturn(el);

        AppPluginLinkRequest req = AppPluginLinkRequest.builder().appName(ea.getMetadata().getName())
                .pluginName(ep.getMetadata().getName()).build();

        mvc.perform(
                post("/app-plugin-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(HalUtils.halMapper().writeValueAsString(req))
                        .accept(HAL_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/app-plugin-links/" + el.getMetadata().getName())));
    }

    @Test
    void shouldDeleteLink() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        plugin.getStatus().updateDeploymentPhase(EntandoDeploymentPhase.FAILED, 1L);
        when(entandoLinkService.findByName(eq(el.getMetadata().getName())))
                .thenReturn(Optional.of(el));
        when(entandoPluginService.findByName(eq(plugin.getMetadata().getName())))
                .thenReturn(Optional.of(plugin));

        mvc.perform(delete("/app-plugin-links/{name}", el.getMetadata().getName()))
                .andExpect(status().isNoContent());
        Mockito.verify(entandoLinkService, times(1)).delete(any(EntandoAppPluginLink.class));
        Mockito.verify(entandoPluginService, never()).deletePlugin(any(EntandoPlugin.class));

        plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        plugin.getStatus().updateDeploymentPhase(EntandoDeploymentPhase.STARTED, 1L);
        when(entandoPluginService.findByName(eq(plugin.getMetadata().getName())))
                .thenReturn(Optional.of(plugin));
        mvc.perform(delete("/app-plugin-links/{name}", el.getMetadata().getName()))
                .andExpect(status().isNoContent());
        Mockito.verify(entandoPluginService, never()).deletePlugin(any(EntandoPlugin.class));
        Mockito.verify(entandoPluginService, never()).scaleDownPlugin(any(EntandoPlugin.class));
    }

    @Test
    void shouldDeleteLinkAndScaleDownThePluginDeployment() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        plugin.getStatus().updateDeploymentPhase(EntandoDeploymentPhase.FAILED, 1L);
        when(entandoLinkService.findByName(el.getMetadata().getName()))
                .thenReturn(Optional.of(el));
        when(entandoPluginService.findByName(plugin.getMetadata().getName()))
                .thenReturn(Optional.of(plugin));

        mvc.perform(delete("/app-plugin-links/delete-and-scale-down/{name}", el.getMetadata().getName()))
                .andExpect(status().isNoContent());
        Mockito.verify(entandoLinkService, times(1)).delete(any(EntandoAppPluginLink.class));
        Mockito.verify(entandoPluginService, never()).deletePlugin(any(EntandoPlugin.class));
        Mockito.verify(entandoPluginService, never()).scaleDownPlugin(any(EntandoPlugin.class));

        plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        plugin.getStatus().updateDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL, 1L);
        when(entandoPluginService.findByName(plugin.getMetadata().getName()))
                .thenReturn(Optional.of(plugin));
        mvc.perform(delete("/app-plugin-links/delete-and-scale-down/{name}", el.getMetadata().getName()))
                .andExpect(status().isNoContent());
        Mockito.verify(entandoPluginService, never()).deletePlugin(any(EntandoPlugin.class));
        Mockito.verify(entandoPluginService, times(1)).scaleDownPlugin(any(EntandoPlugin.class));
    }

    @Test
    void shouldDeleteLinkButKeepWorkingPlugin() throws Exception {
        EntandoAppPluginLink el = EntandoLinkTestHelper.getTestLink();
        EntandoPlugin plugin = EntandoPluginTestHelper.getTestEntandoPlugin();
        plugin.getStatus().updateDeploymentPhase(EntandoDeploymentPhase.SUCCESSFUL, 1L);
        when(entandoLinkService.findByName(eq(el.getMetadata().getName())))
                .thenReturn(Optional.of(el));
        when(entandoPluginService.findByName(eq(plugin.getMetadata().getName())))
                .thenReturn(Optional.of(plugin));

        mvc.perform(delete("/app-plugin-links/{name}", el.getMetadata().getName()))
                .andExpect(status().isNoContent());
        ArgumentCaptor<EntandoPlugin> argCapt = ArgumentCaptor.forClass(EntandoPlugin.class);
        Mockito.verify(entandoLinkService, times(1)).delete(any(EntandoAppPluginLink.class));
        Mockito.verify(entandoPluginService, times(0)).deletePlugin(any(EntandoPlugin.class));
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        when(entandoLinkService.findByName(anyString())).thenReturn(Optional.empty());

        mvc.perform(get("/app-plugin-links/any-name")
                .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound());
    }

}
