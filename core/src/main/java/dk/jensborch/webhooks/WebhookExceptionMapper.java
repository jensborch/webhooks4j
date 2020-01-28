package dk.jensborch.webhooks;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS exception mapper for {@link WebhookException}.
 */
@Provider
public class WebhookExceptionMapper implements ExceptionMapper<WebhookException> {

    @Override
    public Response toResponse(final WebhookException e) {
        return Response.status(e.getError().getCode().getStatus())
                .entity(Entity.json(e.getError()))
                .build();
    }
}
