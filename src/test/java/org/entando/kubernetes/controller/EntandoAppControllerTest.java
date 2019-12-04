package org.entando.kubernetes.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.model.app.EntandoApp;
import org.entando.kubernetes.service.EntandoAppService;
import org.entando.kubernetes.util.EntandoAppTestHelper;
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
public class EntandoAppControllerTest {

    public static final String TEST_NAME = "my-app";
    public static final String TEST_NAMESPACE = "my-namespace";
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EntandoAppService entandoAppService;

    @Test
    public void shouldReturnEmptyListIfNotAppIsDeployed() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .build().toUri();

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json("{}"));

        verify(entandoAppService, times(1)).listEntandoApps();
    }

    @Test
    public void shouldReturnAListWithOneApp() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_NAMESPACE)
                .build().toUri();

        EntandoApp tempApp = EntandoAppTestHelper.getTestEntandoApp(TEST_NAME);
        tempApp.getMetadata().setNamespace(TEST_NAMESPACE);
        when(entandoAppService.listEntandoAppsInNamespace(any(String.class))).thenReturn(Collections.singletonList(tempApp));

        mvc.perform(get(uri).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$._embedded.entandoAppList").isNotEmpty())
                .andExpect(jsonPath("$._embedded.entandoAppList[0].metadata.name" ).value(TEST_NAME))
                .andExpect(jsonPath("$._embedded.entandoAppList[0].metadata.namespace").value(TEST_NAMESPACE));


        verify(entandoAppService, times(1)).listEntandoAppsInNamespace(TEST_NAMESPACE);
    }

    @Test
    public void shouldReturn404IfAppNotFound() throws Exception {
        URI uri = UriComponentsBuilder
                .fromUriString(EntandoAppTestHelper.BASE_APP_ENDPOINT)
                .pathSegment(TEST_NAMESPACE, TEST_NAME)
                .build().toUri();
        mvc.perform(get(uri)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

}
