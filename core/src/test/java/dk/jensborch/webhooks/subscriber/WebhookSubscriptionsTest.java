package dk.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookSubscriptions}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookSubscriptionsTest {

    @Mock
    private Client client;

    @Mock
    private Invocation.Builder builder;

    @Mock
    private Response response;

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
        lenient().when(builder.post(any(Entity.class))).thenReturn(response);
        lenient().when(builder.delete()).thenReturn(response);
        lenient().when(response.hasEntity()).thenReturn(true);
    }

    @Test
    public void testRegister() throws Exception {
        subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister() throws Exception {
        subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister404() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        when(response.readEntity(any(Class.class))).thenReturn(new WebhookError(WebhookError.Code.NOT_FOUND, "test"));
        subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE));
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testUnregister500() {
        when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        when(response.readEntity(any(Class.class))).thenReturn(new WebhookError(WebhookError.Code.REGISTER_ERROR, "test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE)));
        verify(repo, times(2)).save(any());
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testUnregisterProcessingException() {
        when(builder.delete()).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.unsubscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testRegisterProcessingException() {
        when(builder.post(any(Entity.class))).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions
                .subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                        .state(Webhook.State.SUBSCRIBE))
        );
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
    }

    @Test
    public void testRegisterHttp400() {
        when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        when(response.getStatus()).thenReturn(404);
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", WebhookError.Code.REGISTER_ERROR);
        when(response.readEntity(any(GenericType.class))).thenReturn(map);
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE)));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
        assertEquals("Failed to register, got HTTP status code 404 and error: {code=REGISTER_ERROR}", e.getError().getMsg());
    }

    @Test
    public void testRegisterHttp500() {
        when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        when(response.readEntity(any(GenericType.class))).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> subscriptions.subscribe(new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), "test_topic")
                .state(Webhook.State.SUBSCRIBE)));
        assertEquals(WebhookError.Code.REGISTER_ERROR, e.getError().getCode());
        assertEquals("Failed to register, got HTTP status code 0 and error: {msg=test}", e.getError().getMsg());
    }

}
