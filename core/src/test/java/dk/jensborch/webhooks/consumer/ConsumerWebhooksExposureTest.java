package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link ConsumerWebhooksExposure}.
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerWebhooksExposureTest {

    @Mock
    private WebhookRegistry registry;

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
        verify(registry).registre(webhook);
    }

    @Test
    public void testList() {
        Response result = exposure.list("test_topic");
        assertNotNull(result);
    }

    @Test
    public void testGet() {
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
    }

}
