package dk.jensborch.webhooks.consumer;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
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
    private WebhookRepository repo;

    @InjectMocks
    private WebhookRegistry registry;

        @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        Response response = mock(Response.class);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.ACCEPTED);
        lenient().when(builder.post(any(Entity.class))).thenReturn(response);
    }
    
    @Test
    public void testRegistre() throws Exception {
        registry.registre(new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic"));
        verify(repo, times(1)).save(any());
    }
    
}
