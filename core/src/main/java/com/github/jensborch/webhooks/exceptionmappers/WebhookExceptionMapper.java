package com.github.jensborch.webhooks.exceptionmappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.github.jensborch.webhooks.WebhookException;

/**
 * JAX-RS exception mapper for {@link WebhookException}.
 */
@Provider
public class WebhookExceptionMapper implements ExceptionMapper<WebhookException> {

    @Override
    public Response toResponse(final WebhookException e) {
        return Response.status(e.getError().getCode().getStatus())
                .entity(e.getError())
                .build();
    }
}
