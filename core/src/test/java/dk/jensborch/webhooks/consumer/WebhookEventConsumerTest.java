package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;
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

    @BeforeEach
    public void setUp() throws Exception {
        publisherUri = new URI("http://publisher.dk");
        consumerUri = new URI("http://consumer.dk");
        Webhook webhook = new Webhook(publisherUri, consumerUri, "test_topic");
        lenient().when(registry.findByPublisher(any(URI.class))).thenReturn(Optional.of(webhook));
        lenient().when(event.select(ArgumentMatchers.<Class<WebhookEvent>>any(), any(EventTopicLiteral.class))).thenReturn(event);
        lenient().when(repo.save(any())).then(returnsFirstArg());
        Optional<Webhook> publishers = Optional.of(webhook);
        lenient().when(registry.findByPublisher(publisherUri)).thenReturn(publishers);
    }

    @Test
    public void testReceive() throws Exception {
        WebhookEvent callbackEvent = new WebhookEvent("test_topic", new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent, publisherUri);
        assertNotNull(status, "Exposure must return a response");
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testReceiveTwice() throws Exception {
        WebhookEvent callbackEvent = new WebhookEvent("test_topic", new HashMap<>());
        when(repo.findByEventId(any()))
                .thenReturn(
                        Optional.of(
                                new ProcessingStatus(callbackEvent, UUID.randomUUID())
                                        .done(true))
                );
        consumer.consume(callbackEvent, publisherUri);
        verify(repo, times(0)).save(any());
    }

    @Test
    public void testReceiveException() throws Exception {
        doThrow(new ObserverException("Test")).when(event).fire(any());
        WebhookEvent callbackEvent = new WebhookEvent("test_topic", new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent, publisherUri);
        assertNotNull(status, "Exposure must return a response wehn ProcessingException is thrown");
        verify(repo, times(2)).save(any());
    }

}
