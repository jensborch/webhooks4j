package com.github.jensborch.webhooks.subscriber;


import static org.exparity.hamcrest.date.ZonedDateTimeMatchers.after;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.SortedSet;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.publisher.Publisher;
import com.github.jensborch.webhooks.publisher.PublisherStatusRepository;
import io.quarkus.test.junit.QuarkusTest;
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
        Webhook webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TestEventListener.TOPIC);
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        WebhookEventStatus s1 = new WebhookEventStatus(new WebhookEvent(TestEventListener.TOPIC, new HashMap<>()).webhook(webhook.getId()));
        WebhookEventStatus s2 = new WebhookEventStatus(new WebhookEvent(TestEventListener.TOPIC, new HashMap<>()).webhook(webhook.getId()));
        pubStatusRepo.save(s1);
        pubStatusRepo.save(s2);
        SortedSet<WebhookEventStatus> events = pubStatusRepo.list(ZonedDateTime.now().minusMinutes(1), TestEventListener.TOPIC);
        assertThat(events.size(), greaterThan(1));
        assertThat(events, hasItems(s2, s1));
        consumer.sync(webhook);
        webhook = subscriptions.find(webhook.getId()).get();
        assertThat(webhook.getUpdated(), after(webhook.getCreated()));
        assertFalse(subStatusRepo.list(from, webhook.getId()).stream().filter(s -> s.getStatus() == WebhookEventStatus.Status.FAILED).findAny().isPresent());
        assertThat(listener.getEvents().keySet(), hasItems(s2.getId(), s1.getId()));
    }

}
