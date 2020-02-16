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
import dk.jensborch.webhooks.repository.WebhookRepository;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
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
    private StatusRepository statusRepo;

    public void publish(@NotNull @Valid final WebhookEvent e) {
        LOG.debug("Publishing event {}", e);
        repo.list(e.getTopic())
                .stream()
                .filter(w -> w.getTopics().contains(e.getTopic()))
                .filter(Webhook::isActive)
                .forEach(w -> call(w, e));
    }

    private void call(final Webhook webhook, final WebhookEvent event) {
        LOG.debug("Publishing to {}", webhook);
        ProcessingStatus status = statusRepo.save(new ProcessingStatus(event, webhook.getId()));
        try {
            Response response = client.target(webhook.getConsumer())
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
