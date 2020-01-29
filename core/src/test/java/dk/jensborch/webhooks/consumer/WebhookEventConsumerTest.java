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

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;

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
    private StatusRepository repo;

    @InjectMocks
    private WebhookEventConsumer consumer;

    @BeforeEach
    public void setUp() {
        lenient().when(event.select(ArgumentMatchers.<Class<WebhookEvent>>any(), any(EventTopicLiteral.class))).thenReturn(event);
        lenient().when(repo.save(any())).then(returnsFirstArg());
    }

    @Test
    public void testReceive() throws Exception {
        WebhookEvent callbackEvent = new WebhookEvent("topic", new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent, new URI("http://test.dk"));
        assertNotNull(status, "Exposure must return a response");
        verify(repo, times(2)).save(any());
    }

    @Test
    public void testReceiveTwice() throws Exception {
        WebhookEvent callbackEvent = new WebhookEvent("topic", new HashMap<>());
        when(repo.findByEventId(any()))
                .thenReturn(
                        Optional.of(
                                new ProcessingStatus(callbackEvent, new URI("http://test.dk"))
                                        .done(true))
                );
        consumer.consume(callbackEvent, new URI("http://test.dk"));
        verify(repo, times(0)).save(any());
    }

    @Test
    public void testReceiveException() throws Exception {
        doThrow(new ObserverException("Test")).when(event).fire(any());
        WebhookEvent callbackEvent = new WebhookEvent("topic", new HashMap<>());
        ProcessingStatus status = consumer.consume(callbackEvent, new URI("http://test.dk"));
        assertNotNull(status, "Exposure must return a response wehn ProcessingException is thrown");
        verify(repo, times(2)).save(any());
    }

}
