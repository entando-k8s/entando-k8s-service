package org.entando.kubernetes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.KubernetesNamespaceService;
import org.entando.kubernetes.service.assembler.EntandoPluginResourceAssembler;
import org.entando.kubernetes.service.assembler.KubernetesNamespaceResourceAssembler;
import org.entando.kubernetes.util.EntandoPluginTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.ThrowableProblem;

@Tag("component")
public class KubernetesNamespaceControllerTest {

    private EntandoPluginController pluginController;
    private EntandoPluginResourceAssembler pluginResourceAssembler = new EntandoPluginResourceAssembler();

    private KubernetesNamespaceController nsController;
    private KubernetesNamespaceResourceAssembler nsRa;
    private KubernetesNamespaceService nsService;


    @BeforeEach
    public void setup() {
        nsService = mock(KubernetesNamespaceService.class);
        pluginController = mock(EntandoPluginController.class);
        nsRa = new KubernetesNamespaceResourceAssembler();
        nsController = new KubernetesNamespaceController(nsRa, nsService);
    }

    @Test
    public void shouldReturnOkResponseAndLinks() {
        when(nsService.getObservedNamespaceList()).thenReturn(Collections.emptyList());
        assertThat(nsController.list().getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(nsController.list().getBody()).isNotNull();
        assertThat(nsController.list().getBody().getLink("plugins").isPresent()).isTrue();
        assertThat(nsController.list().getBody().getLink("apps").isPresent()).isTrue();
        assertThat(nsController.list().getBody().getLink("bundles").isPresent()).isTrue();
    }

    @Test
    public void shouldThrowAnExceptionWhenAskingForNotObservedNamespace() {
       ThrowableProblem tp = Assertions.assertThrows(ThrowableProblem.class, () -> {
           when(nsService.getObservedNamespace(anyString())).thenReturn(Optional.empty());
           nsController.getByName("any-namespace");
       });
       assertThat(tp.getStatus().getStatusCode()).isEqualTo(404);
    }

    @Test
    public void shouldReturnSelfLink() {
       when(nsService.getObservedNamespace("my-name")) .thenReturn(Optional.of(testNamespace()));
       ResponseEntity<EntityModel<Namespace>> re = nsController.getByName("my-name");
       assertThat(re.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
       assertThat(re.getBody()).isNotNull();
       assertThat(re.getBody().getLinks()).isNotNull();
       assertThat(re.getBody().getLinks().getLink("self").isPresent()).isTrue();
    }

    @Test
    public void shouldReturnLinksToEntandoCustomResourceInNamespace() {
        CollectionModel<EntityModel<EntandoPlugin>> epList = new CollectionModel<>(
               testPluginList().stream().map(pluginResourceAssembler::toModel).collect(Collectors.toList())
        );
        when(nsService.getObservedNamespace(TEST_PLUGIN_NAMESPACE)) .thenReturn(Optional.of(testNamespace()));
        when(pluginController.listInNamespace(TEST_PLUGIN_NAMESPACE)).thenReturn(ResponseEntity.ok(epList));

        ResponseEntity<EntityModel<Namespace>> re = nsController.getByName(TEST_PLUGIN_NAMESPACE);
        assertThat(re.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(re.getBody()).isNotNull();
        assertThat(re.getBody().getLinks()).isNotNull();
        assertThat(re.getBody().getLinks().getLink("plugins").isPresent()).isTrue();
        assertThat(re.getBody().getLinks().getLink("apps").isPresent()).isTrue();
        assertThat(re.getBody().getLinks().getLink("bundles").isPresent()).isTrue();

    }

    @Test
    public void shouldThrowExceptionForInvalidNamespaceName() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        String invalidNamespace = "Access-Control-Allow-Origin: *";
        ThrowableProblem tp = Assertions.assertThrows(ThrowableProblem.class, () -> {
            nsController.listAppsInNamespace(invalidNamespace, resp);
        });
        assertThat(tp.getStatus()).isNotNull();
        assertThat(tp.getStatus().getStatusCode()).isEqualTo(400);

        tp = Assertions.assertThrows(ThrowableProblem.class, () -> {
            nsController.listBundlesInNamespace(invalidNamespace, resp);
        });
        assertThat(tp.getStatus()).isNotNull();
        assertThat(tp.getStatus().getStatusCode()).isEqualTo(400);

        tp = Assertions.assertThrows(ThrowableProblem.class, () -> {
            nsController.listPluginsInNamespace(invalidNamespace, resp);
        });
        assertThat(tp.getStatus()).isNotNull();
        assertThat(tp.getStatus().getStatusCode()).isEqualTo(400);
    }

    @Test
    public void redirectsToOtherControllers() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        String namespace = "entando";
        nsController.listAppsInNamespace(namespace, resp);
        verify(resp, times(1)).setStatus(302);
        verify(resp, times(1)).setHeader("Location", "/apps?namespace=entando");

        resp = mock(HttpServletResponse.class);
        nsController.listPluginsInNamespace(namespace, resp);
        verify(resp, times(1)).setStatus(302);
        verify(resp, times(1)).setHeader("Location", "/plugins?namespace=entando");

        resp = mock(HttpServletResponse.class);
        nsController.listBundlesInNamespace(namespace, resp);
        verify(resp, times(1)).setStatus(302);
        verify(resp, times(1)).setHeader("Location", "/bundles?namespace=entando");
    }

    private Namespace testNamespace() {
        return new NamespaceBuilder().withNewMetadata().withName("my-name").endMetadata().build();
    }

    private List<EntandoPlugin>  testPluginList() {
        return Collections.singletonList(EntandoPluginTestHelper.getTestEntandoPlugin());
    }
}
