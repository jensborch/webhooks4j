package dk.jensborch.webhooks.consumer;

import java.util.SortedSet;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventStatus;
import dk.jensborch.webhooks.WebhookEventTopic;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Webhook event consumer.
 */
@Dependent
public class WebhookEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookEventConsumer.class);

    @Inject
    Event<WebhookEvent> event;

    @Inject
    @Consumer
    WebhookEventStatusRepository repo;

    @Inject
    WebhookRegistry registry;

    @Inject
    @Consumer
    Client client;

    /**
     * Consume a callback webhook event and fire CDI events.
     *
     * @param callbackEvent to process
     * @return Processing status for the event
     */
    public WebhookEventStatus consume(final WebhookEvent callbackEvent) {
        LOG.debug("Receiving event {}", callbackEvent);
        Webhook webhook = findPublisher(callbackEvent);
        WebhookEventStatus status = findOrCreate(callbackEvent, webhook);
        if (status.eligible()) {
            try {
                event
                        .select(WebhookEvent.class, new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
                repo.save(status.done(true));
                registry.touch(webhook.getId());
                LOG.debug("Done processing event {}", callbackEvent);
            } catch (ObserverException e) {
                LOG.warn("Error processing event {}", callbackEvent, e);
                repo.save(status.done(false));
            }
        }
        return status;
    }

    /**
     * Synchronize with old events from publisher.
     *
     * @param webhook to synchronize.
     */
    public void sync(final Webhook webhook) {
        try {
            Response response = client.target(webhook.getPublisher())
                    .queryParam("from", webhook.getUpdated())
                    .queryParam("webhook", webhook.getId())
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                response
                        .readEntity(new GenericType<SortedSet<WebhookEventStatus>>() {
                        })
                        .stream()
                        .map(WebhookEventStatus::getEvent).forEach(this::consume);
            } else {
                String msg = "Error synchronizing old events, got HTTP status code " + response.getStatus() + " for webhook: " + webhook;
                LOG.warn(msg);
                throw new WebhookException(new WebhookError(WebhookError.Code.SYNC_ERROR, msg));
            }
        } catch (ProcessingException e) {
            String msg = "Processing error synchronizing old events for webhook: " + webhook;
            LOG.warn(msg, e);
            throw new WebhookException(new WebhookError(WebhookError.Code.SYNC_ERROR, msg), e);
        }
    }

    private Webhook findPublisher(final WebhookEvent callbackEvent) {
        return registry
                .find(callbackEvent.getPublisher())
                .filter(w -> w.getTopics().contains(callbackEvent.getTopic()))
                .filter(Webhook::isActive)
                .orElseThrow(() -> new WebhookException(
                new WebhookError(
                        WebhookError.Code.UNKNOWN_PUBLISHER,
                        "Unknown/inactive publisher " + callbackEvent.getPublisher() + " for topic " + callbackEvent.getTopic()))
                );
    }

    private WebhookEventStatus findOrCreate(final WebhookEvent callbackEvent, final Webhook webhook) {
        return repo
                .find(callbackEvent.getId())
                .orElseGet(() -> repo.save(new WebhookEventStatus(callbackEvent, webhook.getId())));
    }

    /**
     * CDI annotation literal for event topics.
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
