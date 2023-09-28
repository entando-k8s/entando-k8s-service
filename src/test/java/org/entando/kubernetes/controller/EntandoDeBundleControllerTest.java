package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAME;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.exception.NotFoundExceptionFactory;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
class EntandoDeBundleControllerTest {

    private MockMvc mvc;

    @MockBean
    private EntandoDeBundleService entandoDeBundleService;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturnEmptyListIfNotBundleIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();
        when(entandoDeBundleService.getAll("primary")).thenReturn(Collections.emptyList());

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoDeBundleService, times(1)).getAll("primary");
    }

    @Test
    void shouldReturnAListWithOneBundle() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("primary");
        when(entandoDeBundleService.getAll("primary")).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundles").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.name").value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE))
                .andExpect(jsonPath("$._links", hasKey("bundle")))
                .andExpect(jsonPath("$._links", hasKey("bundles-list")));

        verify(entandoDeBundleService, times(1)).getAll("primary");
    }

    @Test
    void shouldReturnAListFilteredByRepoUrl() throws Exception {
        final String repoUrl = "https://github.com/entando-samples/standard-demo-content-bundle.git";
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("repoUrl", Optional.of(repoUrl))
                .queryParam("tenantCode", "tenant1")
                .build().toUri();

        EntandoDeBundle tempBundle1 = EntandoDeBundleTestHelper.getTestEntandoDeBundle("tenant1");
        EntandoDeBundle tempBundle2 = EntandoDeBundleTestHelper.getTestEntandoDeBundle(repoUrl, "tenant1");
        when(entandoDeBundleService.getAll("tenant1")).thenReturn(Arrays.asList(tempBundle1, tempBundle2));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundles").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.name").value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE))
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].spec.tags[0].tarball").value(repoUrl))
                .andExpect(jsonPath("$._links", hasKey("bundle")))
                .andExpect(jsonPath("$._links", hasKey("bundles-list")));

        verify(entandoDeBundleService, times(1)).getAll("tenant1");
    }

    @Test
    void shouldReturnAListWithOneBundleWhenSearchingInANamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("namespace", TEST_BUNDLE_NAMESPACE)
                .queryParam("tenantCode", "tenant2")
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("tenant2");
        when(entandoDeBundleService.getAllInNamespace(anyString(), eq("tenant2"))).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundles").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.name").value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundles[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));

        verify(entandoDeBundleService, times(1)).getAllInNamespace(TEST_BUNDLE_NAMESPACE, "tenant2");
    }

    @Test
    void shouldReturnAListWithOneBundleWhenFilteringByNameAndNamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .pathSegment(TEST_BUNDLE_NAME)
                .queryParam("tenantCode", "primary")
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("primary");
        when(entandoDeBundleService.findByName(anyString(), eq("primary"))).thenReturn(Optional.of(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name").value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$.metadata.namespace").value(TEST_BUNDLE_NAMESPACE));

        verify(entandoDeBundleService, times(1))
                .findByName(TEST_BUNDLE_NAME, "primary");

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

    }

    @Test
    void shouldCreateBundle() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("tenantCode", "primary")
                .build().toUri();

        EntandoDeBundle bundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle("primary");
        when(entandoDeBundleService.createBundle(any(), eq("primary"))).thenReturn(bundle);

        mvc.perform(post(uri).accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bundle))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name").value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$.metadata.namespace").value(TEST_BUNDLE_NAMESPACE));
    }

    @Test
    void shouldDeleteBundle() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .path("/" + EntandoDeBundleTestHelper.TEST_BUNDLE_NAME)
                .queryParam("tenantCode", "primary")
                .build().toUri();

        mvc.perform(delete(uri).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void shouldReturn404WhenDeletingNotExistingBundle() throws Exception {

        String bundleName = "not-existing";

        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .path("/" + bundleName)
                .queryParam("tenantCode", "tenant45")
                .build().toUri();

        doThrow(NotFoundExceptionFactory.entandoDeBundle(bundleName)).when(entandoDeBundleService).deleteBundle(any(), any());

        mvc.perform(delete(uri).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
