package org.entando.kubernetes.controller;

import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAME;
import static org.entando.kubernetes.util.EntandoDeBundleTestHelper.TEST_BUNDLE_NAMESPACE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import org.entando.kubernetes.model.debundle.EntandoDeBundle;
import org.entando.kubernetes.service.EntandoDeBundleService;
import org.entando.kubernetes.util.EntandoDeBundleTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EntandoDeBundleControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoDeBundleService entandoDeBundleService;


    @Test
    public void shouldReturnEmptyListIfNotBundleIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoDeBundleService, times(1)).getAllBundles();
    }

    @Test
    public void shouldReturnAListWithOneBundle() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle();
        when(entandoDeBundleService.getAllBundles()).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1)).getAllBundles();
    }

    @Test
    public void shouldReturnAListWithOneBundleWhenFilteringByNamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("namespace", TEST_BUNDLE_NAMESPACE)
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle();
        when(entandoDeBundleService.getAllBundlesInNamespace(anyString())).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1)).getAllBundlesInNamespace(TEST_BUNDLE_NAMESPACE);
    }

    @Test
    public void shouldReturnAListWithOneBundleWhenFilteringByName() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("name", TEST_BUNDLE_NAME)
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle();
        when(entandoDeBundleService.findBundlesByName(anyString())).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1))
                .findBundlesByName(TEST_BUNDLE_NAME);
    }

    @Test
    public void shouldReturnAListWithOneBundleWhenFilteringByNameAndNamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("name", TEST_BUNDLE_NAME)
                .queryParam("namespace", TEST_BUNDLE_NAMESPACE)
                .build().toUri();

        EntandoDeBundle tempBundle = EntandoDeBundleTestHelper.getTestEntandoDeBundle();
        when(entandoDeBundleService.findBundlesByNameAndNamespace(anyString(), anyString())).thenReturn(Collections.singletonList(tempBundle));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.name" ).value(TEST_BUNDLE_NAME))
                .andExpect(jsonPath("$._embedded.entandoDeBundleList[0].metadata.namespace").value(TEST_BUNDLE_NAMESPACE));


        verify(entandoDeBundleService, times(1))
                .findBundlesByNameAndNamespace(TEST_BUNDLE_NAME, TEST_BUNDLE_NAMESPACE);
    }
    @Test
    public void shouldReturnAnEmptyListWhenGettingBundlesInEmptyNamespace() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoDeBundleTestHelper.BASE_BUNDLES_ENDPOINT)
                .queryParam("namespace", "namespaceA")
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));


        verify(entandoDeBundleService, times(1)).getAllBundlesInNamespace("namespaceA");
    }

}
