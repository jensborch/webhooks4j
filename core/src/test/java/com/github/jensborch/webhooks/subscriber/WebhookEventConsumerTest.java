package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.TypeLiteral;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.subscriber.WebhookEventConsumer.EventTopicLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link com.github.jensborch.webhooks.subscriber.WebhookEventConsumer}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookEventConsumerTest {

    private static final String TEST_TOPIC = "test_topic";

    @Mock
    private Client client;

    @Mock
    private Event<WebhookEvent> event;

    @Mock
    private WebhookSubscriptions subscriptions;

    @Mock
    private WebhookEventStatusRepository repo;

    @InjectMocks
    private WebhookEventConsumer consumer;

    private Webhook webhook;

    @BeforeEach
    public void setUp() throws Exception {
        webhook = new Webhook(new URI("http://publisher.dk"), new URI("http://subscriber.dk"), TEST_TOPIC);
        lenient().when(subscriptions.find(any(UUID.class))).thenReturn(Optional.of(webhook));
        lenient().when(subscriptions.findActiveByTopic(any(WebhookEvent.class))).thenReturn(Optional.of(webhook));
        lenient().when(event.select(any(EventTopicLiteral.class))).thenReturn(event);
        lenient().when(repo.save(any())).then(returnsFirstArg());
        Optional<Webhook> publishers = Optional.of(webhook);
        lenient().when(subscriptions.find(any(UUID.class))).thenReturn(publishers);
    }

    @Test
    public void testReceive() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(publisher);
        WebhookEventStatus status = consumer.consume(callbackEvent);
        assertNotNull(status);
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testReceiveTwice() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(publisher);
        when(repo.find(any()))
                .thenReturn(Optional.of(new WebhookEventStatus(callbackEvent)
                        .done(true))
            );
        consumer.consume(callbackEvent);
        verify(repo, times(0)).save(any());
    }

    @Test
    public void testReceiveFindThrowsException() {
        UUID publisher = UUID.randomUUID();
        when(subscriptions.findActiveByTopic(any(WebhookEvent.class))).thenReturn(Optional.empty());
        WebhookEvent callbackEvent = new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(publisher);
        WebhookException e = assertThrows(WebhookException.class, () -> consumer.consume(callbackEvent));
        assertEquals(WebhookError.Code.UNKNOWN_PUBLISHER, e.getError().getCode());
        assertEquals("Unknown/inactive publisher " + publisher + " for topic test_topic", e.getError().getDetail());
    }

    @Test
    public void testReceiveUnknownTopic() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent("unknown_topic", new HashMap<>()).webhook(publisher);
        when(subscriptions.findActiveByTopic(any(WebhookEvent.class))).thenThrow(new WebhookException(new WebhookError(WebhookError.Code.UNKNOWN_PUBLISHER, "test")));
        WebhookException e = assertThrows(WebhookException.class, () -> consumer.consume(callbackEvent));
        assertEquals(WebhookError.Code.UNKNOWN_PUBLISHER, e.getError().getCode());
    }

    @Test
    public void testReceiveException() {
        UUID publisher = UUID.randomUUID();
        doThrow(new ObserverException("Test")).when(event).fire(any());
        WebhookEvent callbackEvent = new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(publisher);
        WebhookEventStatus status = consumer.consume(callbackEvent);
        assertNotNull(status);
        verify(repo, times(2)).save(any());
    }

    public void setupSyncResponse(final WebhookEventStatus... status) {
        WebTarget target = mock(WebTarget.class);
        when(client.target(any(URI.class))).thenReturn(target);
        when(target.queryParam(any(String.class), any())).thenReturn(target);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        Response response = mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        Invocation invocation = mock(Invocation.class);
        when(builder.buildGet()).thenReturn(invocation);
        when(invocation.invoke()).thenReturn(response);

        SortedSet<WebhookEventStatus> statusSet = new TreeSet<>();
        if (status != null && status.length > 0) {
            Event<?> cdiEvent = mock(Event.class);
            doReturn(cdiEvent).when(event).select(any(EventTopicLiteral.class));
            statusSet.addAll(Arrays.asList(status));
        }
        when(response.readEntity(ArgumentMatchers.<GenericType<SortedSet>>any())).thenReturn(statusSet);
    }

    @Test
    public void testSyncNoData() {
        setupSyncResponse();
        consumer.sync(webhook);
        verify(event, times(0)).select(ArgumentMatchers.<TypeLiteral<WebhookEvent>>any(), any(EventTopicLiteral.class));
    }

    @Test
    public void testSync() {
        WebhookEventStatus status = new WebhookEventStatus(new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(webhook.getId()));
        setupSyncResponse(status);
        consumer.sync(webhook);
        verify(event, times(1)).select(any(EventTopicLiteral.class));
    }

}
