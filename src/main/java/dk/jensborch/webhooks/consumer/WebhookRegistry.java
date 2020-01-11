package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
@Dependent
public class WebhookRegistry {

    @Inject
    private Client client;

    public void registre(Webhook webhook) {
        client.target(webhook.getPublisher())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(webhook, MediaType.APPLICATION_JSON));
    }

}
