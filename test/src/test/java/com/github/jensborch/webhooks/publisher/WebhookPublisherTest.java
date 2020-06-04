package com.github.jensborch.webhooks.publisher;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.subscriber.TestEventListener;
import com.github.jensborch.webhooks.subscriber.WebhookSubscriptions;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link WebhookPublisher}.
 */
@QuarkusTest
class WebhookPublisherTest {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    TestEventListener listener;

    @Inject
    WebhookPublisher publisher;

    @Test
    void testSubscribe() throws Exception {
        String uri = "http://localhost:" + ConfigProvider.getConfig().getOptionalValue("quarkus.http.test-port", String.class).orElse("8081");
        Webhook webhook = new Webhook(new URI(uri), new URI(uri), TestEventListener.TOPIC);
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        Map<String, Object> data = new HashMap<>();
        WebhookEvent event = new WebhookEvent(TestEventListener.TOPIC, data).webhook(webhook.getId());
        publisher.publish(event);
        assertThat(listener.getCount(), greaterThan(0));
        assertThat(listener.getEvents().keySet(), hasItems(event.getId()));
    }

}
