package dk.jensborch.webhooks.publisher;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventStatus;
import dk.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import dk.jensborch.webhooks.repositories.WebhookRepository;
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
                .forEach(w -> call(w, event));
    }

    private void call(final Webhook webhook, final WebhookEvent event) {
        LOG.debug("Publishing to {}", webhook);
        WebhookEventStatus status = statusRepo.save(new WebhookEventStatus(event, webhook.getId()));
        try {
            Response response = client.target(webhook.getSubscriber())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(event));
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                LOG.debug("Done publishing to {}", webhook);
                statusRepo.save(status.done(true));
            } else {
                LOG.warn("Error publishing event {} to {} got HTTP error response {}", event, webhook, response.getStatus());
                String error = WebhookError.parseErrorResponseToString(response);
                LOG.warn("Error response is {}", error);
                statusRepo.save(status.done(false));
            }
        } catch (ProcessingException e) {
            LOG.warn("Error publishing to {} got error processing response", webhook, e);
            statusRepo.save(status.done(false));
        }
    }

}
