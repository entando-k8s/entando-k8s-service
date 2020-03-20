package org.entando.kubernetes.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

public class HalUtils {

    public static ObjectMapper halMapper() {
        ObjectMapper halMapper = new ObjectMapper();
        halMapper.registerModule(new Jackson2HalModule());
        halMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return halMapper;
    }
}
