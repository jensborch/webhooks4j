package dk.jensborch.webhooks.status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.util.HashMap;
import java.util.TreeSet;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ProcessingStatus}.
 */
public class ProcessingStatusTest {

    private Webhook webhook;
    private ProcessingStatus status;

    @BeforeEach
    public void setUp() throws Exception {
        URI publisherUri = new URI("http://publisher.dk");
        URI consumerUri = new URI("http://consumer.dk");
        webhook = new Webhook(publisherUri, consumerUri, "test");
        status = new ProcessingStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
    }

    @Test
    public void testDone() {
        ProcessingStatus result = status.done(true);
        assertEquals(ProcessingStatus.Status.SUCCESS, status.getStatus());
    }

    @Test
    public void testEligible() {
        assertTrue(status.eligible());
    }

    @Test
    public void testCompareTo() {
        ProcessingStatus status2 = new ProcessingStatus(new WebhookEvent(webhook.getId(), "test", new HashMap<>()), webhook.getId());
        assertTrue(status.compareTo(status2) > 0);
        TreeSet<ProcessingStatus> list = new TreeSet<>();
        list.add(status);
        list.add(status2);
        assertEquals(status2, list.stream().findFirst().get());
    }

    @Test
    public void testGetEnd() {
        assertNull(status.getEnd());
    }

}
