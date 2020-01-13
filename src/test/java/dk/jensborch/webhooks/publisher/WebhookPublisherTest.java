package dk.jensborch.webhooks.publisher;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;
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

    @InjectMocks
    private WebhookPublisher publisher;

    @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        lenient().when(statusRepo.save(any())).then(returnsFirstArg());
    }

    @Test
    public void testNoPublishers() {
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).find(eq(TOPIC));
    }

    @Test
    public void testPublish() throws Exception {
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new HashSet<>()));
        when(repo.find(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).find(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

}
