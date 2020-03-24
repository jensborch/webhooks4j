package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.core.MediaType;

import com.github.jensborch.webhooks.WebhookEvent;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link  WebhookEventMessageBodyReader}.
 */
public class WebhookEventMessageBodyReaderTest {

    @Test
    public void testIsReadable() {
        WebhookEventMessageBodyReader reader = new WebhookEventMessageBodyReader();
        assertTrue(reader.isReadable(WebhookEvent.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }


}
