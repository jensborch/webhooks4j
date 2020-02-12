package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.GenericType;
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

    private HashMap<String, Object> map;

    @BeforeEach
    public void setUp() {
        map = new HashMap<>();
        when(response.readEntity(any(GenericType.class))).thenReturn(map);
    }

    @Test
    public void testParseErrorResponse() {
        Map<String, Object> result = WebhookError.parseErrorResponse(response);
        assertEquals(map, result);
    }

    @Test
    public void testParseErrorResponseToString() {
        String result = WebhookError.parseErrorResponseToString(response);
        assertEquals("{}", result);
    }

}
