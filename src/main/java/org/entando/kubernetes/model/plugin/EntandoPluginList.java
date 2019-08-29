package org.entando.kubernetes.model.plugin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.fabric8.kubernetes.client.CustomResourceList;

@JsonSerialize
@JsonDeserialize
public class EntandoPluginList extends CustomResourceList<EntandoPlugin> {
}
