package dk.jensborch.webhooks.consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.enterprise.event.Event;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.consumer.CallbackExposure.EventTopicLiteral;
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
public class CallbackExposureTest {

    @Mock
    private Event<WebhookEvent> event;

    @Mock
    private StatusRepository repo;

    @InjectMocks
    private CallbackExposure exposure;

    @BeforeEach
    public void setUp() {
        when(event.select(ArgumentMatchers.<Class<WebhookEvent>>any(), any(EventTopicLiteral.class))).thenReturn(event);
        when(repo.save(any())).then(returnsFirstArg());
    }

    @Test
    public void testReceive() {
        WebhookEvent callbackEvent = new WebhookEvent("topic", new HashMap<>());
        Response result = exposure.receive(callbackEvent);
        assertNotNull(result);
        verify(repo, times(2)).save(any());
    }

}
