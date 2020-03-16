package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Webhook}.
 */
public class WebhookTest {

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testSubscribeEndpoints() throws Exception {
        Webhook.SubscriberEndpoint sub = new Webhook.SubscriberEndpoint(new URI("http://test.dk"));
        assertEquals("http://test.dk/subscriber-events", sub.getEvents().toString());
        assertEquals("http://test.dk/subscriber-webhooks", sub.getWebhooks().toString());
    }

    @Test
    public void testPublisherEndpoints() throws Exception {
        Webhook.PublisherEndpoint sub = new Webhook.PublisherEndpoint(new URI("http://test.dk/test"));
        assertEquals("http://test.dk/test/publisher-events", sub.getEvents().toString());
        assertEquals("http://test.dk/test/publisher-webhooks", sub.getWebhooks().toString());
    }

}
