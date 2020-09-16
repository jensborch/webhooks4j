package com.github.jensborch.webhooks.publisher;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookResponseHandler;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Webhook publisher.
 */
@Dependent
public class WebhookPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookPublisher.class);

    @Inject
    @Publisher
    Client client;

    @Inject
    @Publisher
    WebhookRepository repo;

    @Inject
    @Publisher
    private WebhookEventStatusRepository statusRepo;

    /**
     * Publish a webhook event.
     *
     * @param event to publish.
     */
    public void publish(@NotNull @Valid final WebhookEvent event) {
        LOG.debug("Publishing event {}", event);
        repo.list(event.getTopic())
                .stream()
                .filter(w -> w.getTopics().contains(event.getTopic()))
                .filter(Webhook::isActive)
                .forEach(w -> call(w, event.webhook(w.getId())));
    }

    @SuppressWarnings("PMD.InvalidLogMessageFormat")
    private void call(final Webhook webhook, final WebhookEvent event) {
        LOG.debug("Publishing to {}", webhook);
        WebhookEventStatus status = statusRepo.save(new WebhookEventStatus(event));
        WebhookResponseHandler
                .type(Response.class)
                .invocation(client.target(webhook.subscriberEndpoints().getEvents())
                        .request(MediaType.APPLICATION_JSON)
                        .buildPost(Entity.json(event)))
                .success(r -> {
                    LOG.debug("Done publishing to {}", webhook);
                    statusRepo.save(status.done(true));
                })
                .error(e -> {
                    LOG.warn("Error publishing event {} to {} got error response {}", event, webhook, e);
                    statusRepo.save(status.done(false));
                })
                .exception(e -> {
                    LOG.warn("Error publishing to {} got error processing response", webhook, e);
                    statusRepo.save(status.done(false));
                })
                .invoke();
    }

}
