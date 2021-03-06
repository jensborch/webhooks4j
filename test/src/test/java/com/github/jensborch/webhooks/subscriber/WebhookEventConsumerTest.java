package com.github.jensborch.webhooks.subscriber;


import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.after;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;
import com.github.jensborch.webhooks.publisher.Publisher;
import com.github.jensborch.webhooks.publisher.PublisherStatusRepository;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link WebhookEventConsumer}.
 */
@QuarkusTest
class WebhookEventConsumerTest {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    @Publisher
    PublisherStatusRepository pubStatusRepo;

    @Inject
    @Subscriber
    SubscriberStatusRepository subStatusRepo;

    @Inject
    TestEventListener listener;

    @Inject
    WebhookEventConsumer consumer;

    @Test
    void testSync() throws Exception {
        ZonedDateTime from = ZonedDateTime.now();
        URI uri = new URI("http://localhost:" + ConfigProvider.getConfig().getOptionalValue("quarkus.http.test-port", String.class).orElse("8081"));
        Webhook webhook = new Webhook(uri, uri, TestEventListener.TOPIC);
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        WebhookEventStatus s1 = new WebhookEventStatus(new WebhookEvent(TestEventListener.TOPIC, new HashMap<>()).webhook(webhook.getId()));
        WebhookEventStatus s2 = new WebhookEventStatus(new WebhookEvent(TestEventListener.TOPIC, new HashMap<>()).webhook(webhook.getId()));
        s1.setStatus(WebhookEventStatus.Status.FAILED);
        s2.setStatus(WebhookEventStatus.Status.FAILED);
        pubStatusRepo.save(s1);
        pubStatusRepo.save(s2);
        WebhookEventStatuses events = pubStatusRepo.list(ZonedDateTime.now().minusMinutes(1), null, TestEventListener.TOPIC);
        assertThat(events.getSize(), greaterThan(1));
        assertThat(events.getStatuses(), hasItems(s2, s1));
        consumer.sync(webhook);
        webhook = subscriptions.find(webhook.getId()).get();
        assertThat(webhook.getUpdated().plusMinutes(5), after(webhook.getCreated()));
        assertFalse(subStatusRepo.list(from, null, webhook.getId()).getStatuses().stream().filter(s -> s.getStatus() == WebhookEventStatus.Status.FAILED).findAny().isPresent());
        assertThat(listener.getEvents().keySet(), hasItems(s2.getId(), s1.getId()));
        assertEquals(WebhookEventStatus.Status.SUCCESS, pubStatusRepo.find(s1.getId()).get().getStatus());
        assertEquals(WebhookEventStatus.Status.SUCCESS, pubStatusRepo.find(s2.getId()).get().getStatus());
    }

}
