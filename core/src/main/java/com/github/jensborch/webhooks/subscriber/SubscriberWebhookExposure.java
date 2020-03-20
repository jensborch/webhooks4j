package com.github.jensborch.webhooks.subscriber;

import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEventTopics;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseBuilder;
import com.github.jensborch.webhooks.validation.ValidUUID;

/**
 * Exposure for registration of webhooks.
 */
@Path(Webhook.SubscriberEndpoints.WEBHOOKS_PATH)
@DeclareRoles("subscriber")
@RolesAllowed({"subscriber"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@SuppressWarnings("PMD.ExcessiveImports")
public class SubscriberWebhookExposure {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    WebhookEventConsumer consumer;

    @POST
    public Response create(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        subscriptions.subscribe(webhook);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(SubscriberWebhookExposure.class)
                .path(SubscriberWebhookExposure.class, "get")
                .build(webhook.getId()))
                .build();
    }

    @PUT
    @Path("{id}")
    public Response update(
            @ValidUUID @NotNull @PathParam("id") final String id,
            @NotNull @Valid final Webhook updated,
            @Context final UriInfo uriInfo,
            @Context final Request request) {
        if (!id.equals(updated.getId().toString())) {
            throw new WebhookException(
                    new WebhookError(
                            WebhookError.Code.VALIDATION_ERROR,
                            "Webhook " + id + " does not match id in payload " + updated.getId())
            );
        }
        Webhook webhook = subscriptions.find(updated.getId()).orElseThrow(() -> throwNotFound(updated.getId().toString()));
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(webhook)
                .tag(w -> String.valueOf(w.getUpdated().toInstant().toEpochMilli()))
                .fulfilled(w -> {
                    if (updated.getState() != Webhook.State.SYNCHRONIZE) {
                        throw new WebhookException(new WebhookError(WebhookError.Code.ILLEGAL_STATUS, "Illegal status " + updated.getState()));
                    }
                    consumer.sync(updated);
                    return Response.ok(w);
                })
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@ValidUUID @NotNull @PathParam("id") final String id) {
        subscriptions.unsubscribe(UUID.fromString(id));
        return Response.noContent().build();
    }

    @GET
    public Response list(@QueryParam("topics") final String topics) {
        return WebhookResponseBuilder
                .create()
                .entity(subscriptions.list(WebhookEventTopics.parse(topics).getTopics()))
                .build();
    }

    @GET
    @Path("{id}")
    public Response get(@ValidUUID @NotNull @PathParam("id") final String id, @Context final Request request) {
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(subscriptions
                        .find(UUID.fromString(id))
                        .orElseThrow(() -> throwNotFound(id)))
                .tag(w -> String.valueOf(w.getUpdated().toInstant().toEpochMilli()))
                .build();
    }

    private WebhookException throwNotFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
