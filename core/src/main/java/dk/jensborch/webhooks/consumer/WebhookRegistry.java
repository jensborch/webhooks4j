package dk.jensborch.webhooks.consumer;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
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

    public void registre(@NotNull @Valid final Webhook webhook) {
        repo.save(webhook);
        try {
            Response response = client.target(webhook.getPublisher())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(webhook));
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                repo.save(webhook.status(Webhook.Status.FAILED));
                WebhookError error = response.readEntity(WebhookError.class);
                throwWebhookException("Faild to register, got HTTP status code " + response.getStatus() + " and error: " + error);
            }
        } catch (ProcessingException e) {
            repo.save(webhook.status(Webhook.Status.FAILED));
            throwWebhookException("Faild to register, error processing response", e);
        }
    }

    private void throwWebhookException(final String msg) {
        LOG.error(msg);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTRE_ERROR, msg)
        );
    }

    private void throwWebhookException(final String msg, final Exception e) {
        LOG.error(msg, e);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTRE_ERROR, msg), e
        );
    }

    public Optional<Webhook> find(@NotNull final UUID id) {
        return repo.find(id);
    }

    Optional<Webhook> findByPublisher(@NotNull final URI publisher) {
        return repo.findByPublisher(publisher);
    }

    public Set<Webhook> list(@NotNull @Size(min = 1) final String... topic) {
        return repo.list(topic);
    }

}
