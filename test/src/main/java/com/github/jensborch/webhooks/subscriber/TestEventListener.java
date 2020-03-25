package com.github.jensborch.webhooks.subscriber;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventData;
import com.github.jensborch.webhooks.WebhookEventTopic;

/**
 * CDI observer for test events.
 */
@ApplicationScoped
public class TestEventListener {

    public static final String TOPIC = "test_topic";

    private final Map<UUID, WebhookEvent> events = new ConcurrentHashMap<>();

    public void observe(@Observes @WebhookEventTopic(TOPIC) final WebhookEvent<WebhookEventData> event) {
        events.put(event.getId(), event);
    }

    public int getCount() {
        return events.size();
    }

    public Map<UUID, WebhookEvent> getEvents() {
        return Collections.unmodifiableMap(events);
    }

}
