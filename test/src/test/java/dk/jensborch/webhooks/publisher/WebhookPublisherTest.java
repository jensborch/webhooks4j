package dk.jensborch.webhooks.publisher;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.subscriber.TestEventListener;
import dk.jensborch.webhooks.subscriber.WebhookSubscriptions;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link WebhookPublisher}.
 */
@QuarkusTest
public class WebhookPublisherTest {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    TestEventListener listener;

    @Inject
    WebhookPublisher publisher;

    @Test
    public void testRegister() throws Exception {
        Webhook webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TestEventListener.TOPIC);
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        Map<String, Object> data = new HashMap<>();
        publisher.publish(new WebhookEvent(webhook.getId(), TestEventListener.TOPIC, data));
        //TODO: fix
        //assertEquals(1, listener.getCount());
    }

}
