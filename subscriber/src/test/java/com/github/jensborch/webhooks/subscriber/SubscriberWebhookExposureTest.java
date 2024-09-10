package com.github.jensborch.webhooks.subscriber;

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

import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link SubscriberWebhookExposure}.
 */
@ExtendWith(MockitoExtension.class)
class SubscriberWebhookExposureTest {

    @Mock
    private WebhookSubscriptions subscriptions;

    @Mock
    private WebhookEventConsumer consumer;

    @Mock
    private Request request;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private SubscriberWebhookExposure exposure;

    @BeforeEach
    void setUp() throws Exception {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        lenient().when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class), any(String.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.build(any())).thenReturn(new URI("http://test.dk"));
    }

    @Test
    void testCreate() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic").state(Webhook.State.SUBSCRIBE);
        Response result = exposure.subscribe(webhook, uriInfo);
        assertNotNull(result);
        verify(subscriptions).subscribe(webhook);
    }

    @Test
    void testSync() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic");
        when(subscriptions.find(webhook.getId())).thenReturn(Optional.of(webhook));
        Response result = exposure.update(webhook.getId().toString(), webhook.state(Webhook.State.SYNCHRONIZE), request);
        assertNotNull(result);
        verify(consumer).sync(webhook);
    }

    @Test
    void testUpdateToInvalid() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic");
        String id = webhook.getId().toString();
        Webhook failedWebhook = webhook.state(Webhook.State.FAILED);
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.update(id, failedWebhook, request));
        assertEquals(Response.Status.BAD_REQUEST, result.getError().getCode().getStatus());
    }

    @Test
    void testList() {
        Response result = exposure.list("test_topic");
        assertNotNull(result);
        verify(subscriptions).list("test_topic");
    }

    @Test
    void testGet404() {
        String id = UUID.randomUUID().toString();
        WebhookException e = assertThrows(WebhookException.class, () -> exposure.get(id, request));
        assertEquals(Response.Status.NOT_FOUND, e.getError().getCode().getStatus());
    }

    @Test
    void testGet() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic");
        when(subscriptions.find(any())).thenReturn(Optional.of(webhook));
        Response result = exposure.get(UUID.randomUUID().toString(), request);
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

}
