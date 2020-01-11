package dk.jensborch.webhooks.consumer;

import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventTopic;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;

/**
 *
 */
@Path("/receive-callback")
public class CallbackExposure {

    @Inject
    private Event<WebhookEvent> event;

    @Inject
    private StatusRepository repo;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receive(WebhookEvent callbackEvent) {
        ProcessingStatus status = repo.find(callbackEvent.getId()).orElse(repo.save(new ProcessingStatus(callbackEvent)));
        if (status.eligible()) {
            try {
                event
                        .select(WebhookEvent.class, new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
            } catch (ObserverException e) {
                repo.save(status.end(false));
            }
            repo.save(status.end(true));
        }
        return Response.ok(status).build();
    }

    public static class EventTopicLiteral extends AnnotationLiteral<WebhookEventTopic> implements WebhookEventTopic {

        private final String topic;

        public EventTopicLiteral(String topic) {
            this.topic = topic;
        }

        @Override
        public String value() {
            return topic;
        }
    }
}
