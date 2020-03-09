package dk.jensborch.webhooks.subscriber;

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

import dk.jensborch.webhooks.ResponseHandler;
import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage webhook subscriptions.
 */
@ApplicationScoped
public class WebhookSubscriptions {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookSubscriptions.class);

    @Inject
    @Subscriber
    Client client;

    @Inject
    @Subscriber
    WebhookRepository repo;

    /**
     * Register a webhook to receive events from a publisher. This will throw a
     * {@link WebhookError} runtime exception if registration fails.
     *
     * @param webhook to register.
     */
    public void subscribe(@NotNull @Valid final Webhook webhook) {
        if (repo.find(webhook.getId()).filter(w -> w.equals(webhook)).isPresent()) {
            LOG.info("Webhook {} already exists", webhook);
        } else if (webhook.getStatus() == Webhook.State.SUBSCRIBE) {
            repo.save(webhook.state(Webhook.State.SUBSCRIBING));
            try {
                Response response = client.target(webhook.getPublisher())
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(webhook.state(Webhook.State.SUBSCRIBE)));
                if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                    repo.save(webhook.state(Webhook.State.ACTIVE));
                } else {
                    repo.save(webhook.state(Webhook.State.FAILED));
                    String error = WebhookError.parseErrorResponseToString(response);
                    throwWebhookException("Failed to register, got HTTP status code " + response.getStatus() + " and error: " + error);
                }
            } catch (ProcessingException e) {
                repo.save(webhook.state(Webhook.State.FAILED));
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
    public void unsubscribe(@NotNull final UUID id) {
        Webhook w = find(id).orElseThrow(() -> new WebhookException(
                new WebhookError(WebhookError.Code.REGISTER_ERROR, "Webhook with id " + id + " not found")));
        unsubscribe(w);
    }

    public void unsubscribe(@NotNull @Valid final Webhook webhook) {
        repo.save(webhook.state(Webhook.State.UNSUBSCRIBING));
        ResponseHandler
                .type(Response.class)
                .invocation(client
                        .target(webhook.getPublisher())
                        .path("{id}")
                        .resolveTemplate("id", webhook.getId())
                        .request())
                .success(r -> repo.save(webhook.state(Webhook.State.INACTIVE)))
                .webhookError((error, status) -> {
                    if (status == Response.Status.NOT_FOUND && error.getCode() == WebhookError.Code.NOT_FOUND) {
                        LOG.info("Webhook {} not found at publisher", error);
                        repo.save(webhook.state(Webhook.State.INACTIVE));
                    } else {
                        repo.save(webhook.state(Webhook.State.FAILED));
                        throwWebhookException("Failed to unregister, got HTTP status code " + status.getStatusCode() + ", and error: " + error);
                    }
                })
                .exception(e -> {
                    repo.save(webhook.state(Webhook.State.FAILED));
                    throwWebhookException("Failed to unregister, error processing response", e);
                })
                .invokeDelete();
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
