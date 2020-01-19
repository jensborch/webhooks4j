package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
@ApplicationScoped
public class WebhookRegistry {

    @Inject
    @Consumer
    Client client;

    public void registre(final Webhook webhook) {
        client.target(webhook.getPublisher())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(webhook, MediaType.APPLICATION_JSON));
    }

}
