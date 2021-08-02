package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Collections;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.OperatorDeploymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {
                EntandoKubernetesJavaApplication.class,
                TestSecurityConfiguration.class,
                TestKubernetesConfig.class
        })
@ActiveProfiles("test")
@Tag("component")
@WithMockUser
class ObservedNamespaceControllerTest {

    private MockMvc mvc;

    private ObservedNamespaces observedNamespaces;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        observedNamespaces = new ObservedNamespaces(mock(KubernetesUtils.class), Collections.singletonList(TEST_PLUGIN_NAMESPACE),
                OperatorDeploymentType.HELM);
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturnOkResponseAndLinks() throws Exception {
        mvc.perform(get(URI.create("/namespaces")).accept(HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.observedNamespaces").exists())
                .andExpect(jsonPath("$._links.plugins-in-namespace").exists())
                .andExpect(jsonPath("$._links.apps-in-namespace").exists())
                .andExpect(jsonPath("$._links.bundles-in-namespace").exists())
                .andExpect(jsonPath("$._links.app-plugin-links-in-namespace").exists());
    }

    @Test
    void shouldReturnSelfLink() throws Exception {
        mvc.perform(get("/namespaces/{name}", TEST_PLUGIN_NAMESPACE).accept(HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_PLUGIN_NAMESPACE))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void shouldReturnLinksToEntandoCustomResourceInNamespace() throws Exception {
        mvc.perform(get(URI.create("/namespaces/" + TEST_PLUGIN_NAMESPACE)).accept(HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.plugins-in-namespace.href").value(endsWith("/plugins?namespace=" + TEST_PLUGIN_NAMESPACE)))
                .andExpect(jsonPath("$._links.bundles-in-namespace.href").value(endsWith("/bundles?namespace=" + TEST_PLUGIN_NAMESPACE)))
                .andExpect(jsonPath("$._links.apps-in-namespace.href").value(endsWith("/apps?namespace=" + TEST_PLUGIN_NAMESPACE)))
                .andExpect(jsonPath("$._links.app-plugin-links-in-namespace.href")
                        .value(endsWith("/app-plugin-links?namespace=" + TEST_PLUGIN_NAMESPACE)));

    }

    @Test
    void shouldThrowExceptionForInvalidNamespaceName() throws Exception {

        String invalidNamespace = "Access-Control-Allow-Origin: *";
        mvc.perform(get("/namespaces/{name}", invalidNamespace).accept(HAL_JSON_VALUE))
                .andExpect(status().isBadRequest());
    }
}
