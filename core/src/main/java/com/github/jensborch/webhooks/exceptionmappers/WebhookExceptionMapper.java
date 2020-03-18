package com.github.jensborch.webhooks.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
