package dk.jensborch.webhooks.publisher;

import dk.jensborch.webhooks.consumer.TestEventListener;
import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dk.jensborch.webhooks.consumer.WebhookRegistry;
import dk.jensborch.webhooks.publisher.WebhookPublisher;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 *
 */
@QuarkusTest
public class WebhookPublisherIT {

    @Inject
    WebhookRegistry registry;

    @Inject
    TestEventListener listener;

    @Inject
    WebhookPublisher publisher;

    @Test
    public void testRegister() throws Exception {
        registry.registre(new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), TestEventListener.TOPIC));
        Map<String, Object> data = new HashMap<>();
        publisher.publish(new WebhookEvent(TestEventListener.TOPIC, data));
        assertEquals(1, listener.getCount());
    }

}
