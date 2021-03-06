package com.github.jensborch.webhooks.exceptionmappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.ws.rs.core.Response;

import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookException;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookExceptionMapper}.
 */
class WebhookExceptionMapperTest {

    @Test
    void testToResponse() {
        WebhookException e = new WebhookException(new WebhookError(WebhookError.Code.UNKNOWN_ERROR, "test"));
        WebhookExceptionMapper mapper = new WebhookExceptionMapper();
        Response response = mapper.toResponse(e);
        assertNotNull(response);
        WebhookError entity = (WebhookError) response.getEntity();
        assertEquals("test", entity.getDetail());
    }

}
