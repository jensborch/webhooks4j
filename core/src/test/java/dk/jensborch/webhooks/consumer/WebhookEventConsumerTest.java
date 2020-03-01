package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.consumer.WebhookEventConsumer.EventTopicLiteral;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link CallbackExposure].
 */
@ExtendWith(MockitoExtension.class)
public class WebhookEventConsumerTest {

    private static final String TEST_TOPIC = "test_topic";

    @Mock
    private Client client;

    @Mock
    private Event<WebhookEvent> event;

    @Mock
    private WebhookRegistry registry;

    @Mock
    private StatusRepository repo;

    @InjectMocks
    private WebhookEventConsumer consumer;

    private URI publisherUri;
    private URI consumerUri;
    private Webhook webhook;

    @BeforeEach
    public void setUp() throws Exception {
        publisherUri = new URI("http://publisher.dk");
        consumerUri = new URI("http://consumer.dk");
        webhook = new Webhook(publisherUri, consumerUri, TEST_TOPIC);
        lenient().when(registry.find(any())).thenReturn(Optional.of(webhook));
        lenient().when(event.select(ArgumentMatchers.<Class<WebhookEvent>>any(), any(EventTopicLiteral.class))).thenReturn(event);
        lenient().when(repo.save(any())).then(returnsFirstArg());
        Optional<Webhook> publishers = Optional.of(webhook);
        lenient().when(registry.find(any())).thenReturn(publishers);
    }

    @Test
    public void testReceive() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent);
        assertNotNull(status);
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testReceiveTwice() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        when(repo.find(any()))
                .thenReturn(
                        Optional.of(
                                new ProcessingStatus(callbackEvent, UUID.randomUUID())
                                        .done(true))
                );
        consumer.consume(callbackEvent);
        verify(repo, times(0)).save(any());
    }

    @Test
    public void testReceiveUnknownPublisher() {
        UUID publisher = UUID.randomUUID();
        when(registry.find(any())).thenReturn(Optional.empty());
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        WebhookException e = assertThrows(WebhookException.class, () -> consumer.consume(callbackEvent));
        assertEquals(WebhookError.Code.UNKNOWN_PUBLISHER, e.getError().getCode());
        assertEquals("Unknown/inactive publisher " + publisher + " for topic test_topic", e.getError().getMsg());
    }

    @Test
    public void testReceiveUnknownTopic() {
        UUID publisher = UUID.randomUUID();
        WebhookEvent callbackEvent = new WebhookEvent(publisher, "unknown_topic", new HashMap<>());
        WebhookException e = assertThrows(WebhookException.class, () -> consumer.consume(callbackEvent));
        assertEquals(WebhookError.Code.UNKNOWN_PUBLISHER, e.getError().getCode());
        assertEquals("Unknown/inactive publisher " + publisher + " for topic unknown_topic", e.getError().getMsg());
    }

    @Test
    public void testReceiveException() {
        UUID publisher = UUID.randomUUID();
        doThrow(new ObserverException("Test")).when(event).fire(any());
        WebhookEvent callbackEvent = new WebhookEvent(publisher, TEST_TOPIC, new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent);
        assertNotNull(status);
        verify(repo, times(2)).save(any());
    }

    public void setupSyncResponse(ProcessingStatus... status) {
        WebTarget target = mock(WebTarget.class);
        when(client.target(any(URI.class))).thenReturn(target);
        when(target.queryParam(any(String.class), any())).thenReturn(target);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        Response response = mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(builder.get()).thenReturn(response);
        SortedSet<ProcessingStatus> statusSet = new TreeSet<>();
        if (status != null && status.length > 0) {
            Event cdiEvent = mock(Event.class);
            when(event.select(any(Class.class), any(EventTopicLiteral.class))).thenReturn(cdiEvent);
            Arrays.stream(status).forEach(statusSet::add);
        }
        when(response.readEntity(any(GenericType.class))).thenReturn(statusSet);
    }

    @Test
    public void testSyncNoData() {
        setupSyncResponse();
        consumer.sync(ZonedDateTime.now(), webhook);
        verify(event, times(0)).select(any(Class.class), any(EventTopicLiteral.class));
    }

    @Test
    public void testSync() {
        ProcessingStatus status = new ProcessingStatus(new WebhookEvent(webhook.getId(), TEST_TOPIC, new HashMap<>()), webhook.getId());
        setupSyncResponse(status);
        consumer.sync(ZonedDateTime.now(), webhook);
        verify(event, times(1)).select(any(Class.class), any(EventTopicLiteral.class));
    }

}
