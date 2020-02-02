package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.status.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link ConsumerEventExposure}.
 */
@ExtendWith(MockitoExtension.class)
public class ConsumerEventExposureTest {

    @Mock
    private WebhookEventConsumer consumer;

    @Mock
    private StatusRepository repo;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private ConsumerEventExposure exposure;

    @BeforeEach
    public void setUp() throws Exception {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        lenient().when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class), any(String.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.build(any())).thenReturn(new URI("http://test.dk"));
        lenient().when(uriInfo.getRequestUri()).thenReturn(new URI("http://test.dk"));
    }

    @Test
    public void testReceive() {
        WebhookEvent callbackEvent = new WebhookEvent("test_topic", new HashMap<>());
        Response response = exposure.receive(callbackEvent, uriInfo);
        assertNotNull(response);
        verify(consumer).consume(eq(callbackEvent), any(URI.class));
    }

    @Test
    public void testList() {
        ZonedDateTime now = ZonedDateTime.now();
        Response response = exposure.list("test1, test2", now, uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), eq("test1"), eq("test2"));
    }

    @Test
    public void testGet() {
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
    }

}
