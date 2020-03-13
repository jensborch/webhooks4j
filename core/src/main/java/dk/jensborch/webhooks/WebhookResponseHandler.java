package dk.jensborch.webhooks;

import java.util.Objects;
import java.util.function.Consumer;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around the JAX-RS client API to handle responses more easily.
 *
 * Use it a follows:
 *
 * <pre>{@code
 *  WebhookResponseBuilder
 *               .request(request, Webhook.class)
 *               .entity(webhook)
 *               .tag(w -> String.valueOf(w.getUpdated().toEpochSecond()))
 *               .fulfilled(w -> {
 *                   // If preconditions have been fulfilled use this to e.g. update
 *                   return Response.ok(w);
 *               })
 *               .build();
 * }</pre>
 *
 * @param <T> the response type to handle
 */
public final class WebhookResponseHandler<T> {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookResponseHandler.class);

    private final Object type;
    private Consumer<Response> notFoundConsumer;
    private Consumer<T> successConsumer;
    private Consumer<ProcessingException> processingErrorConsumer;
    private Consumer<WebhookError> webhookErrorConsumer;
    private Invocation invocation;

    private WebhookResponseHandler(final Class<T> type) {
        Objects.requireNonNull(type, "Type must be defined");
        this.type = type;
    }

    private WebhookResponseHandler(final GenericType<T> type) {
        Objects.requireNonNull(type, "Type must be defined");
        this.type = type;
    }

    public static <T> WebhookResponseHandler<T> type(final GenericType<T> type) {
        return new WebhookResponseHandler<>(type);
    }

    public static <T> WebhookResponseHandler<T> type(final Class<T> type) {
        return new WebhookResponseHandler<>(type);
    }

    public WebhookResponseHandler<T> invocation(final Invocation invocation) {
        this.invocation = invocation;
        return this;
    }

    public WebhookResponseHandler<T> success(final Consumer<T> consumer) {
        this.successConsumer = consumer;
        return this;
    }

    public WebhookResponseHandler<T> notFound(final Consumer<Response> consumer) {
        this.notFoundConsumer = consumer;
        return this;
    }

    public WebhookResponseHandler<T> exception(final Consumer<ProcessingException> consumer) {
        this.processingErrorConsumer = consumer;
        return this;
    }

    public WebhookResponseHandler<T> error(final Consumer<WebhookError> consumer) {
        this.webhookErrorConsumer = consumer;
        return this;
    }

    @SuppressWarnings("PMD")
    public void invoke() {
        Objects.requireNonNull(invocation, "Invocation handler must be defined");
        Objects.requireNonNull(successConsumer, "Success handler must be defined");
        try {
            Response response = invocation.invoke();
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                if (type instanceof GenericType) {
                    successConsumer.accept(response.readEntity((GenericType<T>) type));
                } else if (type.equals(Response.class)) {
                    successConsumer.accept((T) response);
                } else {
                    successConsumer.accept(response.readEntity((Class<T>) type));
                }
            } else if (response.getStatusInfo() == Response.Status.NOT_FOUND && notFoundConsumer != null) {
                notFoundConsumer.accept(response);
            } else if (webhookErrorConsumer != null) {
                webhookErrorConsumer.accept(WebhookError.parse(response));
            } else {
                LOG.info("Error processing response, got HTTP status code {}", response.getStatus());
            }
        } catch (ProcessingException e) {
            if (processingErrorConsumer != null) {
                processingErrorConsumer.accept(e);
            }
        }
    }
}
