package dk.jensborch.webhooks;

import java.util.Objects;
import java.util.function.Consumer;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

/**
 *
 * @param <T>
 */
public class ResponseHandler<T extends Class> {

    private final Invocation.Builder invocation;
    private final T type;
    private Consumer<Response> error;
    private Consumer<Response> notFound;
    private Consumer<T> success;
    private Consumer<ProcessingException> processingError;

    public ResponseHandler(final T type, final Invocation.Builder invocation) {
        Objects.requireNonNull(invocation, "Invocation handler must be defined");
        Objects.requireNonNull(type, "Type must be defined");
        this.invocation = invocation;
        this.type = type;
    }

    public ResponseHandler success(final Consumer<T> success) {
        this.success = success;
        return this;
    }

    public ResponseHandler notFound(final Consumer<Response> notFound) {
        this.notFound = notFound;
        return this;
    }

    public ResponseHandler processingError(final Consumer<ProcessingException> error) {
        this.processingError = error;
        return this;
    }

    public ResponseHandler error(final Consumer<Response> error) {
        this.error = error;
        return this;
    }

    public void invokeGet() {
        Objects.requireNonNull(success, "Success handler must be defined");
        try {
            Response response = invocation.get();
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                success.accept((T) response.readEntity(type));
            } else if (response.getStatusInfo() == Response.Status.NOT_FOUND && notFound != null) {
                notFound.accept(response);
            } else if (error != null) {
                error.accept(response);
            }
        } catch (ProcessingException e) {
            if (notFound != null) {
                processingError.accept(e);
            }
        }
    }

}
