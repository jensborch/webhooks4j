package dk.jensborch.webhooks.exceptionmappers;

import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.exceptionmappers.WebhookExceptionMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookExceptionMapper}.
 */
public class WebhookExceptionMapperTest {

    @Test
    public void testToResponse() {
        WebhookException e = new WebhookException(new WebhookError(WebhookError.Code.UNKNOWN_ERROR, "test"));
        WebhookExceptionMapper mapper = new WebhookExceptionMapper();
        Response response = mapper.toResponse(e);
        assertNotNull(response);
        WebhookError entity = (WebhookError) response.getEntity();
        assertEquals("test", entity.getMsg());
    }

}
