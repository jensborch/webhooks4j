package com.github.jensborch.webhooks;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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
