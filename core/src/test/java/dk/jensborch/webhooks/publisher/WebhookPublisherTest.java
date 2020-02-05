package dk.jensborch.webhooks.publisher;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.repository.WebhookRepository;
import dk.jensborch.webhooks.status.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link CallbackExposure].
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.UnusedPrivateField")
public class WebhookPublisherTest {

    private static final String TOPIC = "test";

    @Mock
    private Client client;

    @Mock
    private WebhookRepository repo;

    @Mock
    private StatusRepository statusRepo;

    @Mock
    private Response response;

    @InjectMocks
    private WebhookPublisher publisher;

    @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.ACCEPTED);
        lenient().when(builder.post(any(Entity.class))).thenReturn(response);
        lenient().when(statusRepo.save(any())).then(returnsFirstArg());
    }

    @Test
    public void testNoPublishers() {
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
    }

    @Test
    public void testPublish() throws Exception {
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), new HashSet<>()));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

    @Test
    public void testPublishProcessingException() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.readEntity(any(Class.class))).thenThrow(new ProcessingException("test"));
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), new HashSet<>()));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

    @Test
    public void testPublishFailure() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.readEntity(any(Class.class))).thenReturn(new WebhookError(WebhookError.Code.UNKNOWN_PUBLISHER, "test"));
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), new HashSet<>()));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

}
