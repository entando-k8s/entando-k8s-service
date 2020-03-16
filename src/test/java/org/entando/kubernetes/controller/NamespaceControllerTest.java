package org.entando.kubernetes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import java.util.Collections;
import java.util.Optional;
import org.entando.kubernetes.service.NamespaceService;
import org.entando.kubernetes.service.assembler.NamespaceResourceAssembler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.ThrowableProblem;

@Tag("component")
public class NamespaceControllerTest {

    private NamespaceController nsController;
    private NamespaceResourceAssembler nsRa;
    private NamespaceService nsService;

    @BeforeEach
    public void setup() {
        nsService = mock(NamespaceService.class);
        nsRa = new NamespaceResourceAssembler();
        nsController = new NamespaceController(nsRa, nsService);
    }

    @Test
    public void shouldReturnOkResponse() {
        when(nsService.getObservedNamespaceList()).thenReturn(Collections.emptyList());
        assertThat(nsController.list().getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
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
       assertThat(re.getBody().getLinks().getLink("self").isPresent());
    }


    private Namespace testNamespace() {
        return new NamespaceBuilder().withNewMetadata().withName("my-name").endMetadata().build();
    }
}
