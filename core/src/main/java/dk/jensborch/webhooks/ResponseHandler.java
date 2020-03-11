package dk.jensborch.webhooks;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

/**
 *
 * @param <T>
 */
public final class ResponseHandler<T> {

    private final Object type;
    private Consumer<Response> errorConsumer;
    private Consumer<Response> notFoundConsumer;
    private Consumer<T> successConsumer;
    private Consumer<ProcessingException> processingErrorConsumer;
    private BiConsumer<WebhookError, Response.StatusType> webhookErrorConsumer;
    private Invocation invocation;

    private ResponseHandler(final Class<T> type) {
        Objects.requireNonNull(type, "Type must be defined");
        this.type = type;
    }

    private ResponseHandler(final GenericType<T> type) {
        Objects.requireNonNull(type, "Type must be defined");
        this.type = type;
    }

    public static <T> ResponseHandler<T> type(final GenericType<T> type) {
        return new ResponseHandler<>(type);
    }

    public static <T> ResponseHandler<T> type(final Class<T> type) {
        return new ResponseHandler<>(type);
    }

    public ResponseHandler<T> invocation(final Invocation invocation) {
        this.invocation = invocation;
        return this;
    }

    public ResponseHandler<T> success(final Consumer<T> consumer) {
        this.successConsumer = consumer;
        return this;
    }

    public ResponseHandler<T> notFound(final Consumer<Response> consumer) {
        this.notFoundConsumer = consumer;
        return this;
    }

    public ResponseHandler<T> exception(final Consumer<ProcessingException> consumer) {
        this.processingErrorConsumer = consumer;
        return this;
    }

    public ResponseHandler<T> error(final Consumer<Response> consumer) {
        this.errorConsumer = consumer;
        return this;
    }

    public ResponseHandler<T> webhookError(final BiConsumer<WebhookError, Response.StatusType> consumer) {
        this.webhookErrorConsumer = consumer;
        return this;
    }

    @SuppressWarnings("PMD")
    public <E> void invoke() {
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
                webhookErrorConsumer.accept(WebhookError.parse(response), response.getStatusInfo());
            } else if (errorConsumer != null) {
                errorConsumer.accept(response);
            }
        } catch (ProcessingException e) {
            if (processingErrorConsumer != null) {
                processingErrorConsumer.accept(e);
            }
        }
    }
}
