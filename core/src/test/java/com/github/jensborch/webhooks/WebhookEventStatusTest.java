package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookEventStatus}.
 */
public class WebhookEventStatusTest {

    private Webhook webhook;
    private WebhookEventStatus status;

    @BeforeEach
    public void setUp() throws Exception {
        URI publisherUri = new URI("http://publisher.dk");
        URI subscriberUri = new URI("http://subscriber.dk");
        webhook = new Webhook(publisherUri, subscriberUri, "test");
        status = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
    }

    @Test
    public void testDone() {
        WebhookEventStatus result = status.done(true);
        assertEquals(WebhookEventStatus.Status.SUCCESS, result.getStatus());
    }

    @Test
    public void testEligible() {
        assertTrue(status.eligible());
    }

    @Test
    public void testCompareTo() throws Exception {
        TimeUnit.SECONDS.sleep(1);
        WebhookEventStatus status2 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        assertTrue(status.compareTo(status2) > 0);
        SortedSet<WebhookEventStatus> list = new TreeSet<>();
        list.add(status);
        list.add(status2);
        assertEquals(status2, list.stream().findFirst().get());
    }

    @Test
    public void testGetEnd() {
        assertNull(status.getEnd());
    }

    @Test
    public void testToString() {
        WebhookEventStatus s = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        assertThat(s.toString(), startsWith("WebhookEventStatus{end=null, status=STARTED, id=" + s.getId() + ", event=WebhookEvent{id=" + s.getId() + ", webhook=" + s.getEvent().getWebhook() + ", topic=test, data={}}"));
    }

    @Test
    public void testEquals() {
        UUID publisher = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        WebhookEvent w1 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t1", "t2"));
        WebhookEvent w2 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t3", "t4"));
        ZonedDateTime start = ZonedDateTime.now();
        WebhookEventStatus s1 = new WebhookEventStatus(w1.getId(), w1, start, null, WebhookEventStatus.Status.STARTED);
        WebhookEventStatus s2 = new WebhookEventStatus(w2.getId(), w2, start, null, WebhookEventStatus.Status.STARTED);
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testSet() {
        WebhookEventStatus s1 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        WebhookEventStatus s2 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        SortedSet<WebhookEventStatus> set = new TreeSet<>();
        set.add(s1);
        set.add(s2);
        assertEquals(2, set.size());
    }

    @Test
    public void testNotEquals() {
        WebhookEventStatus s1 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        WebhookEventStatus s2 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()));
        assertNotEquals(s1, s2);
        assertNotEquals(null, s1);
        assertNotEquals(new Object(), s1);
    }

}
