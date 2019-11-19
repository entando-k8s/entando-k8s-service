package org.entando.kubernetes.controller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.entando.kubernetes.KubernetesHelpers.getTestEntandoPlugin;
import static org.entando.kubernetes.TestHelpers.extractFromJson;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import org.entando.kubernetes.KubernetesClientMocker;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class EntandoPluginControllerTest {

    private static final String URL = "/plugins";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private KubernetesClient client;

    private KubernetesClientMocker mocker;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        mocker = new KubernetesClientMocker(client);
    }

    @Test
    public void testListEmpty() throws Exception {
        when(mocker.pluginList.getItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    public void testNotFound() throws Exception {
        final String pluginId = "arbitrary-plugin";
        mocker.mockResult(pluginId, null);

        mockMvc.perform(get(String.format("%s/%s", URL, pluginId)))
                .andDo(print()).andExpect(status().isNotFound());

        verify(mocker.mixedOperation, times(1)).inAnyNamespace();
    }

    @Test
    public void testList() throws Exception {
        EntandoPlugin plugin = getTestEntandoPlugin();

        when(mocker.pluginList.getItems()).thenReturn(singletonList(plugin));
        //@Luca I don't understand why using entando-k8s-custom-model changes this to entandoPluginList
        MvcResult result = mockMvc.perform(get(URL))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.entandoPluginList", hasSize(1)))
                .andReturn();

        Resource<EntandoPlugin> pluginResource = extractFromJson(result.getResponse().getContentAsString(),
                "$._embedded.entandoPluginList[0]", new TypeReference<Resource<EntandoPlugin>>() {
                });

        assertThat(pluginResource.getContent()).usingComparatorForType(new Comparator<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                return f.format(o1).compareTo(f.format(o2));
            }
        }, Date.class).isEqualToComparingFieldByFieldRecursively(plugin);
        assertThat(pluginResource.hasLink("self"));
    }

    @Test
    public void testCreate() throws Exception {
        EntandoPlugin plugin = getTestEntandoPlugin();

        when(mocker.namespaceOperations.create(any(EntandoPlugin.class))).thenReturn(plugin);

        mockMvc.perform(post(URL).content(mapper.writeValueAsBytes(plugin)).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metadata.name", equalTo(plugin.getMetadata().getName())))
                .andExpect(jsonPath("$.metadata.namespace", equalTo(plugin.getMetadata().getNamespace())));

    }

    @Test
    public void testDelete() throws Exception {
        EntandoPlugin plugin = getTestEntandoPlugin();

        when(mocker.namespaceOperations.delete(any(EntandoPlugin.class))).thenReturn(true);

        mockMvc.perform(delete(String.format("%s/%s", URL, plugin.getMetadata().getName())))
                .andDo(print())
                .andExpect(status().isAccepted());
    }

}
