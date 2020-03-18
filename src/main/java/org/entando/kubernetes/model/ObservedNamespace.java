package org.entando.kubernetes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

@Getter
@RequiredArgsConstructor
public class ObservedNamespace {

    private final String name;

}
