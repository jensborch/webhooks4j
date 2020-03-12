package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookError}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookErrorTest {

    @Mock
    private Response response;

    @Test
    public void testParse() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(404);
        when(response.readEntity(any(Class.class))).thenReturn("{ \"code\":\"NOT_FOUND\", \"status\":\"404\", \"msg\":\"test\" }");
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(WebhookError.Code.NOT_FOUND, "test"), result);
    }

    @Test
    public void testParseNoEntity() {
        when(response.hasEntity()).thenReturn(false);
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(0, "No entity"), result);
    }

    @Test
    public void testParseAuthError() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(403);
        when(response.readEntity(any(Class.class))).thenReturn("Not allowed");
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(403, "Not allowed"), result);
    }

    @Test
    public void testParseProcessingException() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(any(Class.class))).thenThrow(new ProcessingException("test"));
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(400, "test"), result);
    }

}
