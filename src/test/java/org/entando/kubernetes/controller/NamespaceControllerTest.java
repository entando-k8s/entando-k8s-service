package org.entando.kubernetes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.entando.kubernetes.util.EntandoPluginTestHelper.TEST_PLUGIN_NAMESPACE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entando.kubernetes.model.plugin.EntandoPlugin;
import org.entando.kubernetes.service.NamespaceService;
import org.entando.kubernetes.service.assembler.EntandoPluginResourceAssembler;
import org.entando.kubernetes.service.assembler.NamespaceResourceAssembler;
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
public class NamespaceControllerTest {

    private EntandoPluginController pluginController;
    private EntandoPluginResourceAssembler pluginResourceAssembler = new EntandoPluginResourceAssembler();

    private NamespaceController nsController;
    private NamespaceResourceAssembler nsRa;
    private NamespaceService nsService;



    @BeforeEach
    public void setup() {
        nsService = mock(NamespaceService.class);
        pluginController = mock(EntandoPluginController.class);
        nsRa = new NamespaceResourceAssembler();
        nsController = new NamespaceController(nsRa, nsService);
    }

    @Test
    public void shouldReturnOkResponseAndLinks() {
        when(nsService.getObservedNamespaceList()).thenReturn(Collections.emptyList());
        assertThat(nsController.list().getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(nsController.list().getBody()).isNotNull();
        assertThat(nsController.list().getBody().getLink("all-plugins").isPresent()).isTrue();
        assertThat(nsController.list().getBody().getLink("all-apps").isPresent()).isTrue();
        assertThat(nsController.list().getBody().getLink("all-bundles").isPresent()).isTrue();
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

    private Namespace testNamespace() {
        return new NamespaceBuilder().withNewMetadata().withName("my-name").endMetadata().build();
    }

    private List<EntandoPlugin>  testPluginList() {
        return Collections.singletonList(EntandoPluginTestHelper.getTestEntandoPlugin());
    }
}
