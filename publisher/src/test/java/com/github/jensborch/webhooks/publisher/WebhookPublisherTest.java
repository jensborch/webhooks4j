package com.github.jensborch.webhooks.publisher;

import com.github.jensborch.webhooks.publisher.WebhookPublisher;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link com.github.jensborch.webhooks.publisher.WebhookPublisher}.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.UnusedPrivateField")
class WebhookPublisherTest {

    private static final String TOPIC = "test";

    @Mock
    private Client client;

    @Mock
    private WebhookRepository repo;

    @Mock
    private WebhookEventStatusRepository statusRepo;

    @Mock
    private Response response;

    @Mock
    private Invocation invocation;

    @InjectMocks
    private WebhookPublisher publisher;

    @BeforeEach
    public void setUp() {
        WebTarget target = mock(WebTarget.class);
        Invocation.Builder builder = mock(Invocation.Builder.class);
        lenient().when(target.request(eq(MediaType.APPLICATION_JSON))).thenReturn(builder);
        lenient().when(client.target(any(URI.class))).thenReturn(target);
        lenient().when(response.getStatusInfo()).thenReturn(Response.Status.ACCEPTED);

        lenient().when(builder.buildPost(any(Entity.class))).thenReturn(invocation);
        lenient().when(invocation.invoke()).thenReturn(response);

        lenient().when(statusRepo.save(any())).then(returnsFirstArg());
        lenient().when(response.hasEntity()).thenReturn(true);
    }

    @Test
    public void testNoPublishers() {
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
    }

    @Test
    public void testPublish() throws Exception {
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), TOPIC));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

    @Test
    public void testPublishProcessingException() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenThrow(new ProcessingException("test"));
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), TOPIC));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

    @Test
    public void testPublishFailure() throws Exception {
        when(response.getStatusInfo()).thenReturn(Response.Status.BAD_REQUEST);
        when(response.readEntity(ArgumentMatchers.<Class<String>>any())).thenReturn("{ \"code\":\"VALIDATION_ERROR\", \"status\":\"400\", \"msg\":\"test\" }");
        Set<Webhook> hooks = new HashSet<>();
        hooks.add(new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), TOPIC));
        when(repo.list(TOPIC)).thenReturn(hooks);
        publisher.publish(new WebhookEvent(TOPIC, new HashMap<>()));
        verify(repo, times(1)).list(eq(TOPIC));
        verify(statusRepo, times(2)).save(any());
    }

}
