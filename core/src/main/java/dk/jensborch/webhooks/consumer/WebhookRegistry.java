package dk.jensborch.webhooks.consumer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
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
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consumer webhook registry to find and manipulate webhooks.
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

    /**
     * Register a webhook to receive events from a publisher. This will throw a
     * {@link WebhookError} runtime exception if registration fails.
     *
     * @param webhook to register.
     */
    public void register(@NotNull @Valid final Webhook webhook) {
        if (repo.find(webhook.getId()).filter(w -> w.equals(webhook)).isPresent()) {
            LOG.info("Webhook {} already exists", webhook);
        } else if (webhook.getStatus() == Webhook.Status.REGISTER) {
            repo.save(webhook.status(Webhook.Status.REGISTERING));
            try {
                Response response = client.target(webhook.getPublisher())
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(webhook.status(Webhook.Status.REGISTER)));
                if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                    repo.save(webhook.status(Webhook.Status.ACTIVE));
                } else {
                    repo.save(webhook.status(Webhook.Status.FAILED));
                    String error = WebhookError.parseErrorResponseToString(response);
                    throwWebhookException("Failed to register, got HTTP status code " + response.getStatus() + " and error: " + error);
                }
            } catch (ProcessingException e) {
                repo.save(webhook.status(Webhook.Status.FAILED));
                throwWebhookException("Failed to register, error processing response", e);
            }
        } else {
            throwWebhookException("Status must be REGISTER to register webhook");
        }
    }

    /**
     * Unregister a webhook from a publisher. This will throw a
     * {@link WebhookError} runtime exception if it isn't possible to
     * unregister.
     *
     * @param id of webhook to unregister.
     */
    public void unregister(@NotNull final UUID id) {
        Webhook w = find(id).orElseThrow(() -> new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, "Webhook with id " + id + " not found")));
        unregister(w);
    }

    public void unregister(@NotNull @Valid final Webhook webhook) {
        repo.save(webhook.status(Webhook.Status.UNREGISTERING));
        try {
            Response response = client.target(webhook.getPublisher())
                    .path("{id}")
                    .resolveTemplate("id", webhook.getId())
                    .request()
                    .delete();
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                repo.save(webhook.status(Webhook.Status.INACTIVE));
            } else if (response.getStatusInfo() == Response.Status.NOT_FOUND) {
                handleNotFound(response, webhook);
            } else {
                repo.save(webhook.status(Webhook.Status.FAILED));
                String error = WebhookError.parseErrorResponseToString(response);
                throwWebhookException("Failed to unregister, got HTTP status code " + response.getStatus() + " and error: " + error);
            }
        } catch (ProcessingException e) {
            repo.save(webhook.status(Webhook.Status.FAILED));
            throwWebhookException("Failed to unregister, error processing response", e);
        }
    }

    private void handleNotFound(final Response response, final Webhook webhook) {
        WebhookError error = WebhookError.parseErrorResponse(response);
        if (error.getCode() == WebhookError.Code.NOT_FOUND) {
            LOG.info("Webhook {} not found at publisher", error);
            repo.save(webhook.status(Webhook.Status.INACTIVE));
        } else {
            repo.save(webhook.status(Webhook.Status.FAILED));
            throwWebhookException("Failed to unregister, got HTTP status code 404, but unexpected error code: " + error);
        }
    }

    private void throwWebhookException(final String msg) {
        LOG.error(msg);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, msg)
        );
    }

    private void throwWebhookException(final String msg, final Exception e) {
        LOG.error(msg, e);
        throw new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, msg), e
        );
    }

    public Optional<Webhook> find(final UUID id) {
        return repo.find(id);
    }

    public Set<Webhook> list(final String... topic) {
        return repo.list(topic);
    }

    public void touch(final UUID id) {
        repo.touch(id);
    }

}
