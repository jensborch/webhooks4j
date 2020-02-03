package dk.jensborch.webhooks.consumer;

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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookRegistry}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookRegistryTest {

    @Mock
    private Client client;

    @Mock
    private Invocation.Builder builder;

    @Mock
    private Response response;

    @Mock
    private WebhookRepository repo;

    @InjectMocks
    private WebhookRegistry registry;

    @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.ACCEPTED);
        lenient().when(builder.post(any(Entity.class))).thenReturn(response);
    }

    @Test
    public void testRegistre() throws Exception {
        registry.registre(new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic"));
        verify(repo, times(1)).save(any());
    }

    @Test
    public void testRegistreProcessingException() {
        when(builder.post(any(Entity.class))).thenThrow(new ProcessingException("test"));
        assertThrows(WebhookException.class, () -> {
            registry.registre(new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic"));
        });
    }

    @Test
    public void testRegistreHttp400() {
        when(response.getStatusInfo()).thenReturn(Response.Status.NOT_FOUND);
        when(response.getStatus()).thenReturn(404);
        when(response.readEntity(any(Class.class))).thenReturn(new WebhookError(WebhookError.Code.REGISTRE_ERROR, "test"));
        WebhookException e = assertThrows(WebhookException.class, () -> {
            registry.registre(new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic"));
        });
        assertEquals(WebhookError.Code.REGISTRE_ERROR, e.getError().getCode());
        assertEquals("Faild to register, got HTTP status code 404 and error: WebhookError(code=REGISTRE_ERROR, msg=test)", e.getError().getMsg());
    }

    @Test
    public void testRegistreHttp500() {
        when(response.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
        when(response.readEntity(any(Class.class))).thenThrow(new ProcessingException("test"));
        WebhookException e = assertThrows(WebhookException.class, () -> {
            registry.registre(new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic"));
        });
        assertEquals(WebhookError.Code.REGISTRE_ERROR, e.getError().getCode());
        assertEquals("Faild to register, error processing response", e.getError().getMsg());
    }

}
