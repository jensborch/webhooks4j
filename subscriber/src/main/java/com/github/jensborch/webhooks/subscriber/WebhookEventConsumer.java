package com.github.jensborch.webhooks.subscriber;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObserverException;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;
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
    WebhookSyncConfiguration conf;

    @Inject
    Event<WebhookEvent> event;

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
    public WebhookEventStatus consume(final WebhookEvent callbackEvent) {
        LOG.debug("Receiving event {}", callbackEvent);
        Webhook webhook = findPublisher(callbackEvent);
        WebhookEventStatus status = findOrCreate(callbackEvent);
        try {
            if (status.eligible()) {
                LOG.debug("Processing event {}", callbackEvent);
                event
                        .select(WebhookEvent.class, new EventTopicLiteral(callbackEvent.getTopic()))
                        .fire(callbackEvent);
                repo.save(status.done(true));
                LOG.debug("Done processing event {}", callbackEvent);
            }
            LOG.debug("Updating timestamp on webhook {}", webhook);
            subscriptions.touch(webhook.getId());
        } catch (ObserverException e) {
            LOG.warn("Error processing event {}", callbackEvent, e);
            repo.save(status.done(false));
        }

        return status;
    }

    /**
     * Synchronize with old events from publisher.
     *
     * @param webhook to synchronize.
     */
    public Webhook sync(final Webhook webhook) {
        WebhookResponseHandler
                .type(WebhookEventStatuses.class)
                .invocation(client
                        .target(webhook.publisherEndpoints().getEvents())
                        .queryParam("from", conf.syncFrom(webhook))
                        .queryParam("webhook", webhook.getId())
                        .queryParam("status", WebhookEventStatus.Status.FAILED.toString())
                        .request(MediaType.APPLICATION_JSON)
                        .buildGet())
                .success(s -> s.getStatuses().stream().map(WebhookEventStatus::getEvent).map(this::consume).forEach(e -> updatePublisherStatus(webhook, e)))
                .error(this::handleError)
                .exception(this::handleException)
                .invoke();
        return subscriptions
                .find(webhook.getId())
                .orElseThrow(() -> new WebhookException(
                new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + webhook.getId() + " has been deleted while synchronizing")
        ));
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

    private Webhook findPublisher(final WebhookEvent callbackEvent) {
        return subscriptions
                .find(Optional.ofNullable(callbackEvent.getWebhook()).orElseThrow(() -> noPublisher(callbackEvent)))
                .filter(w -> w.getTopics().contains(callbackEvent.getTopic()))
                .filter(Webhook::isActive)
                .orElseThrow(() -> noPublisher(callbackEvent));
    }

    private WebhookException noPublisher(final WebhookEvent callbackEvent) {
        return new WebhookException(
                new WebhookError(
                        WebhookError.Code.UNKNOWN_PUBLISHER,
                        "Unknown/inactive publisher " + callbackEvent.getWebhook() + " for topic " + callbackEvent.getTopic()));
    }

    private WebhookEventStatus findOrCreate(final WebhookEvent callbackEvent) {
        return repo
                .find(callbackEvent.getId())
                .orElseGet(() -> repo.save(new WebhookEventStatus(callbackEvent)));
    }

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    private void updatePublisherStatus(final Webhook webhook, final WebhookEventStatus status) {
        LOG.debug("Updating status for event: {}", status);
        if (status.getStatus() == WebhookEventStatus.Status.SUCCESS) {
            WebhookResponseHandler
                    .type(WebhookEventStatus.class)
                    .invocation(client
                            .target(webhook.publisherEndpoints().getEvents())
                            .path("{id}")
                            .resolveTemplate("id", status.getId())
                            .request(MediaType.APPLICATION_JSON)
                            .buildPut(Entity.json(status)))
                    .success(e -> LOG.debug("Updated publisher event {}", e))
                    .error(e -> LOG.warn("Faild to update publisher event {}", e))
                    .exception(e -> LOG.warn("Faild to update publisher event", e))
                    .invoke();
        }
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
