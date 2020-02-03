package dk.jensborch.webhooks.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import dk.jensborch.webhooks.repository.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link PublisherWebhookExposure}.
 */
@ExtendWith(MockitoExtension.class)
public class PublisherWebhookExposureTest {

    @Mock
    private WebhookRepository repo;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private PublisherWebhookExposure exposure;

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
        verify(repo).save(webhook);
    }

    @Test
    public void testList() {
        Response result = exposure.list("test_topic");
        assertNotNull(result);
        verify(repo).list("test_topic");
    }

    @Test
    public void testGet404() {
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
        assertEquals(404, result.getStatus());
    }

    @Test
    public void testGet() throws Exception {
        Webhook webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://consumer.dk"), "test_topic");
        when(repo.find(any())).thenReturn(Optional.of(webhook));
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

    @Test
    public void testDelete() {
        UUID id = UUID.randomUUID();
        Response result = exposure.delete(id);
        assertNotNull(result);
        verify(repo).delte(eq(id));
    }

}