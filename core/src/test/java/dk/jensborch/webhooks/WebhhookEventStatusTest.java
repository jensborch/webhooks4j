package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
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
        URI consumerUri = new URI("http://consumer.dk");
        webhook = new Webhook(publisherUri, consumerUri, "test");
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

}
