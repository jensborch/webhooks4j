package com.github.jensborch.webhooks.exceptionmappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.WebhookError;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ConstraintViolationExceptionMapper}.
 */
class ConstraintViolationExceptionMapperTest {

    @Test
    public void testToResponse() {
        Set<? extends ConstraintViolation<?>> constraintViolations = new HashSet<>();
        ConstraintViolationException e = new ConstraintViolationException(constraintViolations);
        ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();
        Response response = mapper.toResponse(e);
        assertNotNull(response);
        WebhookError error = (WebhookError) response.getEntity();
        assertEquals(WebhookError.Code.VALIDATION_ERROR, error.getCode());
    }
}
