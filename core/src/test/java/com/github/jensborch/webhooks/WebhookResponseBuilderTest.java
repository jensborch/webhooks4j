package com.github.jensborch.webhooks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link WebhookResponseBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class WebhookResponseBuilderTest {

    @Mock
    private Request request;

    @Test
    void testBuildNoStore() {
        Response response = WebhookResponseBuilder
                .create(request, String.class)
                .entity("test")
                .build();
        assertEquals(200, response.getStatus());
        assertThat(response.getHeaderString("Cache-Control"), CoreMatchers.containsString("no-store"));
        assertThat(response.getHeaderString(HttpHeaders.VARY), CoreMatchers.containsString(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testBuildNotFulfilled() {
        Response.ResponseBuilder responseBuilder = mock(Response.ResponseBuilder.class);
        when(request.evaluatePreconditions(any(EntityTag.class))).thenReturn(responseBuilder);
        when(responseBuilder.cacheControl(any())).thenReturn(responseBuilder);
        when(responseBuilder.tag(eq(new EntityTag("test")))).thenReturn(responseBuilder);
        when(responseBuilder.header(eq(HttpHeaders.VARY), eq(HttpHeaders.AUTHORIZATION))).thenReturn(responseBuilder);
        WebhookResponseBuilder
                .create(request, String.class)
                .entity("test")
                .fulfilled(Response::ok)
                .tag(e -> e)
                .build();
        CacheControl cache = new CacheControl();
        cache.setMustRevalidate(true);
        verify(responseBuilder).cacheControl(eq(cache));
    }

    @Test
    void testBuildFulfilled() {
        Response response = WebhookResponseBuilder
                .create(request, String.class)
                .entity("test")
                .fulfilled(Response::ok)
                .tag(e -> e)
                .build();
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertThat(response.getHeaderString("Cache-Control"), CoreMatchers.containsString("must-revalidate"));
        assertThat(response.getHeaderString(HttpHeaders.VARY), CoreMatchers.containsString(HttpHeaders.AUTHORIZATION));
    }

}
