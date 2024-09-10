package com.github.jensborch.webhooks.exceptionmappers;

import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.github.jensborch.webhooks.WebhookError;
import jakarta.annotation.Priority;

/**
 * JAX-RS exception mapper for {@link ConstraintViolationException}.
 */
@Provider
@Priority(Priorities.USER - 100)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(final ConstraintViolationException e) {
        WebhookError error = new WebhookError(WebhookError.Code.VALIDATION_ERROR, message(e));
        return Response.status(error.getCode().getStatus())
                .entity(error)
                .build();
    }

    private String message(final ConstraintViolationException e) {
        return e.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ":'" + cv.getMessage() + "'").collect(Collectors.joining(","));
    }
}
