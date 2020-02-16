package dk.jensborch.webhooks;

import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * JAX-RS exception mapper for {@link ConstraintViolationException}.
 */
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
