package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookEventConsumer}.
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerWebhooksExposureTest {

    @Mock
    private WebhookRegistry registry;

    @Mock
    private WebhookEventConsumer consumer;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private ConsumerWebhooksExposure exposure;

    @BeforeEach
    public void setUp() throws Exception {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        lenient().when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class), any(String.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.build(any())).thenReturn(new URI("http://test.dk"));
    }

    @Test
    public void testCreate() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        Response result = exposure.create(webhook, uriInfo);
        assertNotNull(result);
        verify(registry).register(webhook);
    }

    @Test
    public void testUnreg() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        when(registry.find(webhook.getId())).thenReturn(Optional.of(webhook));
        Response result = exposure.update(webhook.state(Webhook.State.UNREGISTER), uriInfo);
        assertNotNull(result);
        verify(registry).unregister(webhook.getId());
    }

    @Test
    public void testSync() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        when(registry.find(webhook.getId())).thenReturn(Optional.of(webhook));
        Response result = exposure.update(webhook.state(Webhook.State.SYNCHRONIZE), uriInfo);
        assertNotNull(result);
        verify(consumer).sync(webhook);
    }

    @Test
    public void testUpdateToInvalid() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        when(registry.find(webhook.getId())).thenReturn(Optional.of(webhook));
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.update(webhook.state(Webhook.State.FAILED), uriInfo));
        assertEquals(Response.Status.BAD_REQUEST, result.getError().getCode().getStatus());
    }

    @Test
    public void testList() {
        Response result = exposure.list("test_topic");
        assertNotNull(result);
        verify(registry).list("test_topic");
    }

    @Test
    public void testGet404() {
        WebhookException e = assertThrows(WebhookException.class, () -> exposure.get(UUID.randomUUID().toString()));
        assertEquals(Response.Status.NOT_FOUND, e.getError().getCode().getStatus());
    }

    @Test
    public void testGet() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        when(registry.find(any())).thenReturn(Optional.of(webhook));
        Response result = exposure.get(UUID.randomUUID().toString());
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

}
