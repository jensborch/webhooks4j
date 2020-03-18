package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
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
public class WebhhookEventStatusTest {

    private Webhook webhook;
    private WebhookEventStatus status;

    @BeforeEach
    public void setUp() throws Exception {
        URI publisherUri = new URI("http://publisher.dk");
        URI subscriberUri = new URI("http://subscriber.dk");
        webhook = new Webhook(publisherUri, subscriberUri, "test");
        status = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
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
        WebhookEventStatus status2 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
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
    public void testToString() throws Exception {
        WebhookEventStatus s = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
        assertThat(s.toString(), startsWith("WebhookEventStatus{end=null, status=STARTED, id=" + s.getId() + ", event=WebhookEvent{id=" + s.getId() + ", publisher=" + s.getEvent().getPublisher() + ", topic=test, data={}}"));
    }

    @Test
    public void testEquals() throws Exception {
        UUID publisher = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        WebhookEvent w1 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t1", "t2"));
        WebhookEvent w2 = new WebhookEvent(id, publisher, "test", Collections.singletonMap("t3", "t4"));
        WebhookEventStatus s1 = new WebhookEventStatus(w1, webhook.getId());
        WebhookEventStatus s2 = new WebhookEventStatus(w2, webhook.getId());
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testNotEquals() throws Exception {
        WebhookEventStatus s1 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
        WebhookEventStatus s2 = new WebhookEventStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
        assertNotEquals(s1, s2);
        assertFalse(s1.equals(null));
        assertFalse(s1.equals(new Object()));
    }

}
