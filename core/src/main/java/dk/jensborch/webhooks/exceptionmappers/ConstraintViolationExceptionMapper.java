package dk.jensborch.webhooks.exceptionmappers;

import dk.jensborch.webhooks.WebhookError;

import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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

    private String message(final ConstraintViolationException exception) {
        return exception.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ":'" + cv.getMessage() + "'").collect(Collectors.joining(","));
    }
}
