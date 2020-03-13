package dk.jensborch.webhooks;

import java.util.Objects;
import java.util.function.Function;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * Utility class for creating conditional PUT and GET responses, with correct
 * cache headers. Use it a follows:
 *
 * <pre>{@code
 *  WebhookResponseBuilder
 *               .request(request, Webhook.class)
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

    private static final CacheControl CACHING = new CacheControl();
    private final Request request;
    private E entity;
    private Function<E, String> tagFunc;
    private Function<E, Response.ResponseBuilder> fulfilledFunc;

    static {
        CACHING.setMustRevalidate(true);
    }

    private WebhookResponseBuilder(final Request request) {
        this.request = request;
    }

    public static <E> WebhookResponseBuilder<E> request(final Request request, final Class<E> clazz) {
        Objects.requireNonNull(request, "Request must not be null");
        return new WebhookResponseBuilder<>(request);
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
        Objects.requireNonNull(tagFunc, "eTag function is required");
        Objects.requireNonNull(entity, "Enitity must not be null");
        Objects.requireNonNull(fulfilledFunc, "Fulfilled functions is required");
        EntityTag etag = new EntityTag(tagFunc.apply(entity));
        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
        if (builder == null) {
            builder = fulfilledFunc.apply(entity);
        }
        return builder
                .cacheControl(CACHING)
                .tag(etag)
                .header(HttpHeaders.VARY, HttpHeaders.AUTHORIZATION)
                .build();
    }
}
