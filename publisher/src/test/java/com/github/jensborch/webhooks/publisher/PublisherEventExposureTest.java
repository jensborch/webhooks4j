package com.github.jensborch.webhooks.publisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
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
class PublisherEventExposureTest {

    @Mock
    private WebhookEventStatusRepository repo;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private Request request;

    @InjectMocks
    private PublisherEventExposure exposure;

    @BeforeEach
    void setUp() throws Exception {
        UriBuilder uriBuilder = mock(UriBuilder.class);
        lenient().when(uriInfo.getBaseUriBuilder()).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.path(any(Class.class), any(String.class))).thenReturn(uriBuilder);
        lenient().when(uriBuilder.build(any())).thenReturn(new URI("http://test.dk"));
        lenient().when(uriInfo.getRequestUri()).thenReturn(new URI("http://test.dk"));
    }

    @Test
    void testListByTopic() {
        when(repo.list(any(ZonedDateTime.class), isNull(), any(String.class))).thenReturn(new WebhookEventStatuses(new TreeSet<>()));
        ZonedDateTime now = ZonedDateTime.now();
        Response response = exposure.list("test1, test2", null, null, now.toString(), uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), isNull(), startsWith("test"), startsWith("test"));
    }

    @Test
    void testListByWebhook() {
        when(repo.list(any(ZonedDateTime.class), isNull(), any(UUID.class))).thenReturn(new WebhookEventStatuses(new TreeSet<>()));
        ZonedDateTime now = ZonedDateTime.now();
        UUID id = UUID.randomUUID();
        Response response = exposure.list(null, id.toString(), null, now.toString(), uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), isNull(), eq(id));
    }

    @Test
    void testGet404() {
        String id = UUID.randomUUID().toString();
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.get(id, request));
        assertEquals(Response.Status.NOT_FOUND, result.getError().getCode().getStatus());
    }

    @Test
    void testGet() {
        WebhookEvent event = new WebhookEvent("test", new HashMap<>());
        when(repo.find(any())).thenReturn(Optional.of(new WebhookEventStatus(event)));
        Response result = exposure.get(UUID.randomUUID().toString(), request);
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

    @Test
    void testPut() {
        WebhookEvent event = new WebhookEvent("test", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event);
        when(repo.find(any())).thenReturn(Optional.of(status));
        Response result = exposure.update(event.getId().toString(), status.done(true), request);
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

    @Test
    void testPutWrongStatus() {
        WebhookEvent event = new WebhookEvent("test", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event);
        String id = event.getId().toString();
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.update(id, status, request));
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST, result.getError().getCode().getStatus());
    }

    @Test
    void testPutWrongId() {
        WebhookEvent event = new WebhookEvent("test", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event).done(true);
        String id = UUID.randomUUID().toString();
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.update(id, status, request));
        assertNotNull(result);
        assertEquals(Response.Status.BAD_REQUEST, result.getError().getCode().getStatus());
    }

    @Test
    void testPut404() {
        WebhookEvent event = new WebhookEvent("test", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event).done(true);
        String id = event.getId().toString();
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.update(id, status, request));
        assertNotNull(result);
        assertEquals(Response.Status.NOT_FOUND, result.getError().getCode().getStatus());
    }

}
