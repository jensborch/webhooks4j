package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link Webhook}.
 */
public class WebhookTest {

    @Test
    public void testCreate() throws Exception {
        Webhook w = new Webhook(UUID.randomUUID(), new URI("http://pub.dk"), new URI("http://sub.dk"), Webhook.State.ACTIVE, null, null, null);
        assertNotNull(w.getTopics());
        assertNotNull(w.getCreated());
        assertNotNull(w.getUpdated());
        assertEquals(w.getUpdated(), w.getCreated());
    }

    @Test
    public void testUpdate() throws Exception {
        ZonedDateTime updated = ZonedDateTime.now();
        Webhook w = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test").updated(updated);
        assertEquals(updated, w.getUpdated());
    }

    @Test
    public void testTouch() throws Exception {
        Webhook w = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        ZonedDateTime old = w.getUpdated();
        TimeUnit.SECONDS.sleep(1);
        assertNotEquals(old, w.touch().getUpdated());
    }

    @Test
    public void testSubscribeEndpoints() throws Exception {
        Webhook.SubscriberEndpoints sub = new Webhook.SubscriberEndpoints(new URI("http://test.dk"));
        assertEquals("http://test.dk/subscriber-events", sub.getEvents().toString());
        assertEquals("http://test.dk/subscriber-webhooks", sub.getWebhooks().toString());
    }

    @Test
    public void testPublisherEndpoints() throws Exception {
        Webhook.PublisherEndpoints sub = new Webhook.PublisherEndpoints(new URI("http://test.dk/test"));
        assertEquals("http://test.dk/test/publisher-events", sub.getEvents().toString());
        assertEquals("http://test.dk/test/publisher-webhooks", sub.getWebhooks().toString());
    }

    @Test
    public void testToString() throws Exception {
        Webhook w = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        assertThat(w.toString(), startsWith("Webhook{state=ACTIVE,"));
    }

    @Test
    public void testEquals() throws Exception {
        Webhook w1 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        Webhook w2 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    public void testNotEquals() throws Exception {
        Webhook w1 = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test");
        Webhook w2 = new Webhook(new URI("http://pub2.dk"), new URI("http://sub2.dk"), "different");
        assertNotEquals(w1, w2);
        assertNotEquals(null, w1);
        assertNotEquals(new Object(), w1);
    }

}
