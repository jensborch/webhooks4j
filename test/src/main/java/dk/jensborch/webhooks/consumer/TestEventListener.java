package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventTopic;

/**
 * CDI observer for test events.
 */
@ApplicationScoped
public class TestEventListener {

    public static final String TOPIC = "test_topic";

    private int count;

    public void test(@Observes @WebhookEventTopic(TOPIC) WebhookEvent event) {
        count = count + 1;
    }

    public int getCount() {
        return count;
    }

}
