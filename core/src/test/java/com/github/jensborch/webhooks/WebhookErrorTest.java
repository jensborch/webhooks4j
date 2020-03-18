package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    @Test
    public void testParseJsonParsingException() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(any(Class.class))).thenReturn("invalid");
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(400, WebhookError.Code.UNKNOWN_ERROR, "invalid"), result);
    }

    @Test
    public void testParseRuntimeException() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(500);
        when(response.readEntity(any(Class.class))).thenThrow(new RuntimeException("test"));
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(500, WebhookError.Code.UNKNOWN_ERROR, "test"), result);
    }

    @Test
    public void testParse401() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(401);
        when(response.readEntity(any(Class.class))).thenReturn("");
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(401, WebhookError.Code.AUTHENTICATION_ERROR, ""), result);
    }

    @Test
    public void testParse403() {
        when(response.hasEntity()).thenReturn(true);
        when(response.getStatus()).thenReturn(403);
        when(response.readEntity(any(Class.class))).thenReturn("");
        WebhookError result = WebhookError.parse(response);
        assertEquals(new WebhookError(403, WebhookError.Code.AUTHORIZATION_ERROR, ""), result);
    }

    @Test
    public void testToString() throws Exception {
        WebhookError w = new WebhookError(WebhookError.Code.NOT_FOUND, "test");
        assertEquals("WebhookError{status=404, code=NOT_FOUND, title=Not found, detail=test}", w.toString());
    }

    @Test
    public void testEquals() throws Exception {
        WebhookError w1 = new WebhookError(WebhookError.Code.AUTHENTICATION_ERROR, "test");
        WebhookError w2 = new WebhookError(WebhookError.Code.AUTHENTICATION_ERROR, "test");
        assertEquals(w1, w2);
        assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    public void testNotEquals() throws Exception {
        WebhookError w1 = new WebhookError(WebhookError.Code.AUTHENTICATION_ERROR, "test");
        WebhookError w2 = new WebhookError(WebhookError.Code.SYNC_ERROR, "test");
        assertNotEquals(w1, w2);
        assertFalse(w1.equals(null));
        assertFalse(w1.equals(new Object()));
    }

}
