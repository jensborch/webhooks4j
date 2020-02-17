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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link PublisherEventExposure}.
 */
@ExtendWith(MockitoExtension.class)
public class PublisherEventExposureTest {

    @Mock
    private StatusRepository repo;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private PublisherEventExposure exposure;

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
    public void testList() {
        ZonedDateTime now = ZonedDateTime.now();
        Response response = exposure.list("test1, test2", now.toString(), uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), eq("test1"), eq("test2"));
    }

    @Test
    public void testGet404() {
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
        assertEquals(404, result.getStatus());
    }

    @Test
    public void testGet() throws Exception {
        UUID publisher = UUID.randomUUID();
        WebhookEvent event = new WebhookEvent(publisher, "test", new HashMap<>());
        when(repo.find(any())).thenReturn(Optional.of(new ProcessingStatus(event, UUID.randomUUID())));
        Response result = exposure.get(UUID.randomUUID());
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

}
