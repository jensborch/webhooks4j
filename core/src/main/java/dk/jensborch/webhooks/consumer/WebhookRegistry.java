package dk.jensborch.webhooks.consumer;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.repository.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@ApplicationScoped
public class WebhookRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookRegistry.class);

    @Inject
    @Consumer
    Client client;

    @Inject
    @Consumer
    WebhookRepository repo;

    public void registre(final Webhook webhook) {
        repo.save(webhook);
        try {
            client.target(webhook.getPublisher())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(webhook, MediaType.APPLICATION_JSON));
        } catch (ProcessingException e) {
            LOG.error("Faild to register");
        }
    }

    public Webhook get(final UUID id) {
        return repo.get(id);
    }

}
