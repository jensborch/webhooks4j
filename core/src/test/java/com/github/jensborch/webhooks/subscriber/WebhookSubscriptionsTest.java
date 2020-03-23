package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookSubscriptions}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookSubscriptionsTest {

    private final static String TEST_TOPIC = "test_topic";

    @Mock
    private Client client;

    @Mock
    private Invocation.Builder builder;

    @Mock
    private Response response;

    @Mock
    private Invocation invocation;

    @Mock
    private WebhookRepository repo;

    @InjectMocks
    private WebhookSubscriptions subscriptions;

    @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(target.request()).thenReturn(builder);
        lenient().when(target.path(any(String.class))).thenReturn(target);
        lenient().when(target.resolveTemplate(any(String.class), any())).thenReturn(target);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.ACCEPTED);
        lenient().when(builder.buildPost(any(Entity.class))).thenReturn(invocation);
        lenient().when(builder.buildDelete()).thenReturn(invocation);
        lenient().when(invocation.invoke()).thenReturn(response);
        lenient().when(response.hasEntity()).thenReturn(true);
    }

    @Test
    public void testFindUnknownPublisher() {
        UUID publisher = UUID.randomUUID();
        when(repo.find(any(UUID.class))).thenReturn(Optional.empty());
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        assertFalse(subscriptions.find(callbackEvent).isPresent());
    }

    @Test
    public void testFindUnknownTopic() throws Exception {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        when(repo.find(any(UUID.class))).thenReturn(Optional.of(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "unknown_topic")));
        assertFalse(subscriptions.find(callbackEvent).isPresent());
    }

    @Test
    public void testRegister() throws Exception {
        subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister() throws Exception {
        subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister404() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        when(response.getStatus()).thenReturn(404);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenReturn("{ \"code\":\"NOT_FOUND\", \"status\":\"404\", \"msg\":\"test\" }");
        subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister500() {
        when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenReturn("{ \"code\":\"REGISTER_ERROR\", \"status\":\"500\", \"msg\":\"test\" }");
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE)));
        verify(repo, times(2)).save(any());
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testUnregisterProcessingException() {
        when(invocation.invoke()).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testRegisterProcessingException() {
        when(invocation.invoke()).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions
                .subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                        .state(Webhook.State.SUBSCRIBE))
        );
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testRegisterHttp400() {
        when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        when(response.getStatus()).thenReturn(404);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenReturn("{ \"code\":\"NOT_FOUND\", \"status\":\"404\", \"msg\":\"test\" }");
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE)));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
        assertEquals("Failed to register, got error response: WebhookError{status=404, code=NOT_FOUND, title=Not found, detail=test}", e.getError().getDetail());
    }

    @Test
    public void testRegisterHttp500() {
        when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE)));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
        assertEquals("Failed to register, got error response: WebhookError{status=0, code=UNKNOWN_ERROR, title=Unknown error, detail=test}", e.getError().getDetail());
    }

}
