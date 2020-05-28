package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.ext.Providers;

import com.github.jensborch.webhooks.WebhookEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link  WebhookEventMessageBodyReader}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookEventMessageBodyReaderTest {

    @Mock
    private WebhookSubscriptions subscriptions;

    @Mock
    private Providers workers;

    @InjectMocks
    private WebhookEventMessageBodyReader reader;

    @Test
    public void testIsReadable() {
        assertTrue(reader.isReadable(WebhookEvent.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void testReadFrom() {
        InputStream entityStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        NoContentException e = assertThrows(NoContentException.class, () -> reader.readFrom(WebhookEvent.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, entityStream));
        assertEquals("No webhook event data", e.getMessage());
    }

}
