package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Webhook}.
 */
class WebhookTest {

    Webhook webhook;

    @BeforeEach
    void setup() throws Exception {
        webhook = new Webhook(UUID.randomUUID(), new URI("http://pub.dk"), new URI("http://sub.dk"), Webhook.State.ACTIVE, null, null, null);
    }

    @Test
    void testConstructor() throws Exception {
        assertNotNull(webhook.getTopics());
        assertNotNull(webhook.getCreated());
        assertNotNull(webhook.getUpdated());
        assertEquals(webhook.getUpdated(), webhook.getCreated());
        assertEquals(Webhook.State.ACTIVE, webhook.getState());
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(Webhook.State.ACTIVE, new Webhook().getState());
    }

    @Test
    void testChangeState() {
        assertEquals(Webhook.State.INACTIVE, webhook.state(Webhook.State.INACTIVE).getState());
    }

    @Test
    void testActive() {
        assertTrue(webhook.state(Webhook.State.SUBSCRIBING).isActive());
        assertFalse(webhook.state(Webhook.State.INACTIVE).isActive());
    }

    @Test
    void testUpdate() throws Exception {
        ZonedDateTime updated = ZonedDateTime.now();
        assertEquals(updated, webhook.updated(updated).getUpdated());
    }

    @Test
    void testTouchNull() throws Exception {
        ZonedDateTime old = webhook.getUpdated().minusMinutes(5).minusSeconds(1);
        assertTrue(old.isBefore(webhook.touch(null).getUpdated()));
    }

    @Test
    void testTouch() throws Exception {
        ZonedDateTime now = ZonedDateTime.now().plusMinutes(5);
        assertEquals(now, webhook.touch(now).getUpdated());
    }

    @Test
    void testSubscribeEndpoints() throws Exception {
        Webhook.Endpoints sub = webhook.subscriberEndpoints();
        assertEquals("http://sub.dk/subscriber-events", sub.getEvents().toString());
        assertEquals("http://sub.dk/subscriber-webhooks", sub.getWebhooks().toString());
    }

    @Test
    void testPublisherEndpoints() throws Exception {
        Webhook.Endpoints sub = webhook.publisherEndpoints();
        assertEquals("http://pub.dk/publisher-events", sub.getEvents().toString());
        assertEquals("http://pub.dk/publisher-webhooks", sub.getWebhooks().toString());
    }

    @Test
    void testToString() throws Exception {
        assertThat(webhook.toString(), startsWith("Webhook{state=ACTIVE,"));
        assertThat(new Webhook().toString(), startsWith("Webhook{state=ACTIVE,"));
    }

    @Test
    void testEquals() throws Exception {
        Webhook w1 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        Webhook w2 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        assertEquals(w1, w2);
        assertEquals(w1, w1);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    void testNotEquals() throws Exception {
        Webhook w1 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        Webhook w2 = new Webhook(new URI("http://pub2.dk"), new URI("http://sub2.dk"), "different");
        assertNotEquals(w1, w2);
        assertNotEquals(null, w1);
        assertNotEquals(new Object(), w1);
    }

}
