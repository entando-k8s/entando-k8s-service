package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoComponentBundleTestHelper.TEST_BUNDLE_NAME;
import static org.entando.kubernetes.util.EntandoComponentBundleTestHelper.TEST_BUNDLE_NAMESPACE;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.EntandoKubernetesJavaApplication;
import org.entando.kubernetes.config.TestJwtDecoderConfig;
import org.entando.kubernetes.config.TestKubernetesConfig;
import org.entando.kubernetes.config.TestSecurityConfiguration;
import org.entando.kubernetes.model.bundle.EntandoComponentBundle;
import org.entando.kubernetes.service.EntandoComponentBundleService;
import org.entando.kubernetes.util.EntandoComponentBundleTestHelper;
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
                TestSecurityConfiguration.class,
                TestKubernetesConfig.class,
                TestJwtDecoderConfig.class
        })
@ActiveProfiles("test")
@Tag("component")
@WithMockUser
public class EntandoComponentBundleControllerTest {

    private MockMvc mvc;


    @MockBean
    private EntandoComponentBundleService entandoDeBundleService;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Test
    public void shouldReturnEmptyListIfNotBundleIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoComponentBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();
        when(entandoDeBundleService.getAll()).thenReturn(Collections.emptyList());

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoDeBundleService, times(1)).getAll();
    }

    @Test
    public void shouldReturnAListWithOneBundle() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoComponentBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();

        EntandoComponentBundle tempBundle = EntandoComponentBundleTestHelper.getTestEntandoComponentBundle();
        when(entandoDeBundleService.getAll()).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoComponentBundles").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoComponentBundles[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoComponentBundles[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE))
                .andExpect(jsonPath("$._links", hasKey("bundle")))
                .andExpect(jsonPath("$._links", hasKey("bundles-in-namespace")));


        verify(entandoDeBundleService, times(1)).getAll();
    }

    @Test
    public void shouldReturnAListWithOneBundleWhenSearchingInANamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoComponentBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("namespace", TEST_BUNDLE_NAMESPACE)
                .build().toUri();

        EntandoComponentBundle tempBundle = EntandoComponentBundleTestHelper.getTestEntandoComponentBundle();
        when(entandoDeBundleService.getAllInNamespace(anyString())).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoComponentBundles").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoComponentBundles[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoComponentBundles[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1)).getAllInNamespace(TEST_BUNDLE_NAMESPACE);
    }

//    public void shouldReturnAListWithOneBundleWhenFilteringByName() throws Exception {
//        URI uri = UriComponentsBuilder
//                .fromUriString(EntandoComponentBundleTestHelper.BASE_BUNDLES_ENDPOINT)
//                .build().toUri();
//
//        EntandoComponentBundle tempBundle = EntandoComponentBundleTestHelper.getTestEntandoComponentBundle();
//        when(entandoDeBundleService.findBundlesByName(anyString())).thenReturn(Collections.singletonList(tempBundle));
//
//        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(jsonPath("$._embedded.entandoDeBundleList").isNotEmpty())
//                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.name" ).value(TEST_BUNDLE_NAME))
//                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));
//
//
//        verify(entandoDeBundleService, times(1))
//                .findBundlesByName(TEST_BUNDLE_NAME);
//    }

    @Test
    public void shouldReturnAListWithOneBundleWhenFilteringByNameAndNamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoComponentBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .pathSegment(TEST_BUNDLE_NAME)
                .build().toUri();

        EntandoComponentBundle tempBundle = EntandoComponentBundleTestHelper.getTestEntandoComponentBundle();
        when(entandoDeBundleService.findByName(anyString())).thenReturn(Optional.of(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$.metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1))
                .findByName(TEST_BUNDLE_NAME);

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

    }

}
