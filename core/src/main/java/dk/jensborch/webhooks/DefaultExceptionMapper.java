package dk.jensborch.webhooks;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Default JAX-RS exception mapper for all exceptions. If your application
 * defines its own exceptions mapper for Exception you should not register this
 * mapper.
 */
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        return Response.status(500)
                .entity(Entity.json(new WebhookError(WebhookError.Code.UNKNOWN_ERROR, e.getMessage())))
                .build();
    }
}
