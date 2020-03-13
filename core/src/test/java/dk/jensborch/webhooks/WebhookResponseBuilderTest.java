package dk.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookResponseBuilder}.
 */
@ExtendWith(MockitoExtension.class)
public class WebhookResponseBuilderTest {

    @Mock
    private Request request;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testBuild() {
        Response response = WebhookResponseBuilder
                .create(request, String.class)
                .entity("test")
                .fulfilled(e -> Response.ok(e))
                .tag(e -> e)
                .build();
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testBuildNotFulfilled() {
        Response.ResponseBuilder responseBuilder = mock(Response.ResponseBuilder.class);
        when(request.evaluatePreconditions(any(EntityTag.class))).thenReturn(responseBuilder);
        when(responseBuilder.cacheControl(any())).thenReturn(responseBuilder);
        when(responseBuilder.tag(eq(new EntityTag("test")))).thenReturn(responseBuilder);
        when(responseBuilder.header(eq(HttpHeaders.VARY), eq(HttpHeaders.AUTHORIZATION))).thenReturn(responseBuilder);
        Response response = WebhookResponseBuilder
                .create(request, String.class)
                .entity("test")
                .fulfilled(e -> Response.ok(e))
                .tag(e -> e)
                .build();
        assertNull(response);
    }

}
