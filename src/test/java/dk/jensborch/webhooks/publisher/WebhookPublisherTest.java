package dk.jensborch.webhooks.publisher;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;

import javax.ws.rs.client.Client;

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
    }

    @Test
    public void testPublish() {
        publisher.publish(new WebhookEvent("test", new HashMap<>()));
        verify(repo, times(1)).find(eq("test"));
    }

}
