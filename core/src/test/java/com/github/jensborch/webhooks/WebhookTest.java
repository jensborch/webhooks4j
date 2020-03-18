package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link Webhook}.
 */
public class WebhookTest {

    @BeforeEach
    public void setUp() {
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
        assertFalse(w1.equals(null));
        assertFalse(w1.equals(new Object()));
    }

}
