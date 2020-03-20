package org.entando.kubernetes.controller;

import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestJwtDecoderConfig;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
public class ApiEntrypointControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldReturnRootLinks() throws Exception {
        mvc.perform(get("/").accept(MediaTypes.HAL_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links",hasKey("apps")))
                .andExpect(jsonPath("$._links",hasKey("plugins")))
                .andExpect(jsonPath("$._links",hasKey("bundles")))
                .andExpect(jsonPath("$._links",hasKey("app-plugin-links")))
                .andExpect(jsonPath("$._links",hasKey("observed-namespaces")));
    }
}
