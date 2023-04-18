package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Collections;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.model.namespace.ObservedNamespaces;
import org.entando.kubernetes.service.KubernetesUtils;
import org.entando.kubernetes.service.OperatorDeploymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
        mvc.perform(get(URI.create("/namespaces")).accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnSelfLink() throws Exception {
        mvc.perform(get("/namespaces/{name}", TEST_PLUGIN_NAMESPACE).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(TEST_PLUGIN_NAMESPACE));
    }

    @Test
    void shouldReturnLinksToEntandoCustomResourceInNamespace() throws Exception {
        mvc.perform(get(URI.create("/namespaces/" + TEST_PLUGIN_NAMESPACE)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldThrowExceptionForInvalidNamespaceName() throws Exception {
        String invalidNamespace = "Access-Control-Allow-Origin: *";
        mvc.perform(get("/namespaces/{name}", invalidNamespace).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
