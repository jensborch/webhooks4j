package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventTopic;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;
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
    StatusRepository repo;

    @Inject
    WebhookRegistry registry;

    public ProcessingStatus consume(final WebhookEvent callbackEvent) {
        LOG.debug("Receiving event {}", callbackEvent);
        Webhook webhook = findPublisher(callbackEvent);
        ProcessingStatus status = findOrCreate(callbackEvent, webhook);
        if (status.eligible()) {
            try {
                event
                        .select(WebhookEvent.class, new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
                repo.save(status.done(true));
                LOG.debug("Done processing event {}", callbackEvent);
            } catch (ObserverException e) {
                LOG.warn("Error processing event {}", callbackEvent, e);
                repo.save(status.done(false));
            }
        }
        return status;
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

    private ProcessingStatus findOrCreate(final WebhookEvent callbackEvent, final Webhook webhook) {
        return repo
                .findByEventId(callbackEvent.getId())
                .orElseGet(() -> repo.save(new ProcessingStatus(callbackEvent, webhook.getId())));
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
