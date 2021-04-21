package org.entando.kubernetes.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class OperatorDeploymentTypeTest {

    @Test
    void shouldSupportOlmAndHelm() {
        //OLM Clustered Scope
        assertThat(OperatorDeploymentType.OLM.isClusterScoped(null)).isTrue();
        assertThat(OperatorDeploymentType.OLM.isClusterScoped(Collections.singletonList(""))).isTrue();
        //OLM Namespace Scope
        assertThat(OperatorDeploymentType.OLM.isClusterScoped(Collections.singletonList("asdf"))).isFalse();
        assertThat(OperatorDeploymentType.OLM.isClusterScoped(Arrays.asList("qwer", "asdf"))).isFalse();
        //Helm Clustered Scope
        assertThat(OperatorDeploymentType.HELM.isClusterScoped(Collections.singletonList("*"))).isTrue();
        //Helm Namespace Scope
        assertThat(OperatorDeploymentType.HELM.isClusterScoped(null)).isFalse();
        assertThat(OperatorDeploymentType.HELM.isClusterScoped(Collections.singletonList("asdf"))).isFalse();
        assertThat(OperatorDeploymentType.HELM.isClusterScoped(Arrays.asList("qwer", "asdf"))).isFalse();

    }
}
