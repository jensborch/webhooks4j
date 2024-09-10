package com.github.jensborch.webhooks;

import java.util.Objects;
import java.util.function.Function;

import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

/**
 * Utility class for creating conditional PUT and GET responses, with correct
 * cache headers. Use it a follows:
 *
 * <pre>{@code
 *  WebhookResponseBuilder
 *               .create(request, Webhook.class)
 *               .entity(webhook)
 *               .tag(w -> String.valueOf(w.getUpdated().toEpochSecond()))
 *               .fulfilled(w -> {
 *                   // If preconditions have been fulfilled use this to e.g. update
 *                   return Response.ok(w);
 *               })
 *               .build();
 * }</pre>
 *
 * @param <E> entity to return in response.
 */
public final class WebhookResponseBuilder<E> {

    private final Request request;
    private E entity;
    private Function<E, String> tagFunc;
    private Function<E, Response.ResponseBuilder> fulfilledFunc = Response::ok;

    private WebhookResponseBuilder(final Request request) {
        this.request = request;
    }

    private WebhookResponseBuilder() {
        this(null);
    }

    public static <E> WebhookResponseBuilder<E> create(final Request request, final Class<E> clazz) {
        return new WebhookResponseBuilder<>(request);
    }

    public static WebhookResponseBuilder<Object> create() {
        return new WebhookResponseBuilder<>();
    }

    public WebhookResponseBuilder<E> entity(final E entity) {
        this.entity = entity;
        return this;
    }

    public WebhookResponseBuilder<E> tag(final Function<E, String> tag) {
        this.tagFunc = tag;
        return this;
    }

    public WebhookResponseBuilder<E> fulfilled(final Function<E, Response.ResponseBuilder> fulfilledFunc) {
        this.fulfilledFunc = fulfilledFunc;
        return this;
    }

    public Response build() {
        Objects.requireNonNull(entity, "Entity must not be null");
        Response.ResponseBuilder builder;
        CacheControl cache;
        if (tagFunc == null) {
            builder = fulfilledFunc.apply(entity);
            cache = noStore();
        } else {
            Objects.requireNonNull(request, "Request must not be null");
            EntityTag etag = new EntityTag(tagFunc.apply(entity));
            builder = request.evaluatePreconditions(etag);
            if (builder == null) {
                builder = fulfilledFunc.apply(entity);
            }
            builder.tag(etag);
            cache = mustRevalidate();
        }
        return builder
                .cacheControl(cache)
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .build();
    }

    private CacheControl mustRevalidate() {
        CacheControl cache = new CacheControl();
        cache.setMustRevalidate(true);
        return cache;
    }

    private CacheControl noStore() {
        CacheControl cache = new CacheControl();
        cache.setNoStore(true);
        return cache;
    }
}
