package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    public void setUp() {
        when(response.hasEntity()).thenReturn(true);
        when(response.readEntity(any(Class.class))).thenReturn("{ \"code\":\"NOT_FOUND\", \"status\":\"404\", \"msg\":\"test\" }");
    }

    @Test
    public void testParse() {
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(WebhookError.Code.NOT_FOUND, "test"), result);
    }

}
