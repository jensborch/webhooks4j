package dk.jensborch.webhooks.consumer;


import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventTopic;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Path("/receive-callback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CallbackExposure {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackExposure.class);

    @Inject
    Event<WebhookEvent> event;

    @Inject
    @Consumer
    StatusRepository repo;

    @POST
    public Response receive(
            final WebhookEvent callbackEvent,
            @Context final UriInfo uriInfo) {
        LOG.debug("Receiving event {}", callbackEvent);
        ProcessingStatus status = repo
                .find(callbackEvent.getId())
                .orElse(repo.save(new ProcessingStatus(callbackEvent, uriInfo.getRequestUri())));
        if (status.eligible()) {
            try {
                event
                        .select(WebhookEvent.class, new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
                repo.save(status.done(true));
            } catch (ObserverException e) {
                repo.save(status.done(false));
            }
        }
        return Response.ok(status).build();
    }

    /**
     *
     */
    public static class EventTopicLiteral extends AnnotationLiteral<WebhookEventTopic> implements WebhookEventTopic {

        private static final long serialVersionUID = -6202789271503219569L;

        private final String topic;

        public EventTopicLiteral(final String topic) {
            super();
            this.topic = topic;
        }

        @Override
        public String value() {
            return topic;
        }
    }
}
