package dk.jensborch.webhooks.publisher;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;

/**
 *
 */
@Dependent
public class WebhookPublisher {

    @Inject
    @Publisher
    Client client;

    @Inject
    @Publisher
    WebhookRepository repo;

    @Inject
    @Publisher
    private StatusRepository statusRepo;

    public void publish(final WebhookEvent e) {
        repo.find(e.getTopic()).forEach(w -> call(w, e));
    }

    private void call(final Webhook webhook, final WebhookEvent event) {
        ProcessingStatus status = statusRepo.save(new ProcessingStatus(event, webhook.getPublisher()));
        try {
            client.target(webhook.getPublisher())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(event, MediaType.APPLICATION_JSON));
            statusRepo.save(status.done(true));
        } catch (ProcessingException e) {
            statusRepo.save(status.done(false));
        }
    }

}
