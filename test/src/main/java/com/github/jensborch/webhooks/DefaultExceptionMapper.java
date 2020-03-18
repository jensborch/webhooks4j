package com.github.jensborch.webhooks;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default JAX-RS exception mapper for all exceptions.
 */
@Provider
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(final Exception e) {
        WebhookError error = new WebhookError(WebhookError.Code.UNKNOWN_ERROR, e.getMessage());
        return Response.status(error.getCode().getStatus())
                .entity(error)
                .build();
    }
}
