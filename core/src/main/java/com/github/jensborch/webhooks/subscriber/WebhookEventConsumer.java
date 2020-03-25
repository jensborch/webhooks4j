package com.github.jensborch.webhooks.subscriber;

import java.util.SortedSet;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventTopic;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseHandler;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Webhook event consumer.
 */
@Dependent
public class WebhookEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookEventConsumer.class);

    @Inject
    Event<WebhookEvent<?>> event;

    @Inject
    @Subscriber
    WebhookEventStatusRepository repo;

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    @Subscriber
    Client client;

    /**
     * Consume a callback webhook event and fire CDI events.
     *
     * @param callbackEvent to process
     * @return Processing status for the event
     */
    public WebhookEventStatus consume(final WebhookEvent<?> callbackEvent) {
        LOG.debug("Receiving event {}", callbackEvent);
        Webhook webhook = subscriptions.findActiveByTopic(callbackEvent).orElseThrow(() -> new WebhookException(
                new WebhookError(
                        WebhookError.Code.UNKNOWN_PUBLISHER,
                        "Unknown/inactive publisher " + callbackEvent.getWebhook() + " for topic " + callbackEvent.getTopic())));
        WebhookEventStatus status = findOrCreate(callbackEvent);
        //Helper<?> helper = Helper.create(webhook.getType());
        if (status.eligible()) {
            try {
                event
                        .select(new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
                repo.save(status.done(true));
                subscriptions.touch(webhook.getId());
                LOG.debug("Done processing event {}", callbackEvent);
            } catch (ObserverException e) {
                LOG.warn("Error processing event {}", callbackEvent, e);
                repo.save(status.done(false));
            }
        }
        return status;
    }

    /**
     *
     * @param <D>
     */
    private static class Helper<D> {

        private final Class<D> type;

        Helper(final Class<D> type) {
            this.type = type;
        }

        static <D> Helper<D> create(final Class<D> type) {
            return new Helper<D>(type);
        }

        @SuppressWarnings("PMD.UnusedFormalParameter")
        TypeLiteral<WebhookEvent<D>> eventTypeLiteral() {
            return new TypeLiteral<WebhookEvent<D>>() {
                private static final long serialVersionUID = 5636572627689425575L;
            };
        }

        <U> WebhookEvent<U> caseEvent(final WebhookEvent<?> callbackEvent) {
            return (WebhookEvent<U>) callbackEvent;
        }

    }

    /**
     * Synchronize with old events from publisher.
     *
     * @param webhook to synchronize.
     */
    public void sync(final Webhook webhook) {
        WebhookResponseHandler
                .type(new GenericType<SortedSet<WebhookEventStatus>>() {
                })
                .invocation(client
                        .target(webhook.gePublisherEndpoints().getEvents())
                        .queryParam("from", webhook.getUpdated())
                        .queryParam("webhook", webhook.getId())
                        .request(MediaType.APPLICATION_JSON)
                        .buildGet())
                .success(events -> events.stream().map(WebhookEventStatus::getEvent).forEach(this::consume))
                .error(this::handleError)
                .exception(this::handleException)
                .invoke();
    }

    private void handleError(final WebhookError error) {
        String msg = "Error synchronizing old events, got error response: " + error.toString();
        LOG.warn(msg);
        throw new WebhookException(new WebhookError(WebhookError.Code.SYNC_ERROR, msg));
    }

    private void handleException(final ProcessingException e) {
        String msg = "Processing error when synchronizing old events";
        LOG.warn(msg, e);
        throw new WebhookException(new WebhookError(WebhookError.Code.SYNC_ERROR, msg), e);
    }

    private WebhookEventStatus findOrCreate(final WebhookEvent<?> callbackEvent) {
        return repo
                .find(callbackEvent.getId())
                .orElseGet(() -> repo.save(new WebhookEventStatus(callbackEvent)));
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
