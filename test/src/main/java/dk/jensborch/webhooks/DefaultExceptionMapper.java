package dk.jensborch.webhooks;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default JAX-RS exception mapper for all exceptions.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        return Response.status(500)
                .entity(Entity.json(new WebhookError(WebhookError.Code.UNKNOWN_ERROR, e.getMessage())))
                .build();
    }
}
