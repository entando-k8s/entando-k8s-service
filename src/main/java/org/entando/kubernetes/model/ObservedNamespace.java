package org.entando.kubernetes.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ObservedNamespace {

    private final String name;

}
