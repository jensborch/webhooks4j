package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link SubscriberEventExposure}.
 */
@ExtendWith(MockitoExtension.class)
public class SubscriberEventExposureTest {

    @Mock
    private WebhookEventConsumer consumer;

    @Mock
    private WebhookEventStatusRepository repo;

    @Mock
    private Request request;

    @Mock
    private UriInfo uriInfo;

    @InjectMocks
    private SubscriberEventExposure exposure;

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
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(publisher, "test_topic", new HashMap<>());
        Response response = exposure.receive(callbackEvent, uriInfo);
        assertNotNull(response);
        verify(consumer).consume(eq(callbackEvent));
    }

    @Test
    public void testListByTopic() {
        ZonedDateTime now = ZonedDateTime.now();
        Response response = exposure.list("test1, test2", null, now.toString(), uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), startsWith("test"), startsWith("test"));
    }

    @Test
    public void testListByWebhook() {
        ZonedDateTime now = ZonedDateTime.now();
        UUID id = UUID.randomUUID();
        Response response = exposure.list(null, id.toString(), now.toString(), uriInfo);
        assertNotNull(response);
        verify(repo).list(eq(now), eq(id));
    }

    @Test
    public void testGet404() {
        WebhookException result = assertThrows(WebhookException.class, () -> exposure.get(UUID.randomUUID().toString(), request));
        assertEquals(Response.Status.NOT_FOUND, result.getError().getCode().getStatus());
    }

    @Test
    public void testGet() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent event = new WebhookEvent(publisher, "test", new HashMap<>());
        when(repo.find(any())).thenReturn(Optional.of(new WebhookEventStatus(event, UUID.randomUUID())));
        Response result = exposure.get(UUID.randomUUID().toString(), request);
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }

}
