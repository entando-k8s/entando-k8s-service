package org.entando.kubernetes.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppPluginLinkRequest {

    private String appName;
    private String pluginName;

}
