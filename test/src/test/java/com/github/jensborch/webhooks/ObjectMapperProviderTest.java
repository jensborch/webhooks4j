package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 *
 */
class ObjectMapperProviderTest {

    @Test
    void testSerialize() throws Exception {
        ObjectMapper mapper = new ObjectMapperProvider().getContext(Object.class);
        String result = mapper.writeValueAsString(new WebhookEvent("test_topic", new HashMap<>()));
        assertNotNull(result);
    }

    @Test
    void testDeserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapperProvider().getContext(Object.class);
        String result = mapper.writeValueAsString(new WebhookEvent("test_topic", new HashMap<>()));
        WebhookEvent hook = mapper.readValue(result, WebhookEvent.class);
        assertNotNull(hook);
    }

}
