package com.github.jensborch.webhooks.subscriber;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventTopic;

/**
 * CDI observer for test events.
 */
@ApplicationScoped
public class TestEventListener {

    public static final String TOPIC = "test_topic";

    private int count;

    public void observe(@Observes @WebhookEventTopic(TOPIC) final WebhookEvent event) {
        count++;
    }

    public int getCount() {
        return count;
    }

}
