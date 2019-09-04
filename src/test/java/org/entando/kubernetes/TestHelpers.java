package org.entando.kubernetes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.io.IOException;

import static com.jayway.jsonpath.JsonPath.using;

public class TestHelpers {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final Configuration JACKSON_JSON_NODE_CONFIGURATION = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();

    public static <T> T extractFromJson(String json, String jsonPath, TypeReference<T> type) throws IOException {
        JsonNode result = using(JACKSON_JSON_NODE_CONFIGURATION).parse(json).read(jsonPath);
        ObjectReader reader = MAPPER.readerFor(type);
        return reader.readValue(result);
    }

}
