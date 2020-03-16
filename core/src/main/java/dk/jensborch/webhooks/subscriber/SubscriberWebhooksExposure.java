package dk.jensborch.webhooks.subscriber;

import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
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

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEventTopics;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.WebhookResponseBuilder;
import dk.jensborch.webhooks.validation.ValidUUID;

/**
 * Exposure for registration of webhooks.
 */
@Path(Webhook.SubscriberEndpoints.WEBHOOKS_PATH)
@DeclareRoles("subscriber")
@RolesAllowed({"subscriber"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SuppressWarnings("PMD.ExcessiveImports")
public class SubscriberWebhooksExposure {

    @Inject
    WebhookSubscriptions subscriper;

    @Inject
    WebhookEventConsumer consumer;

    @POST
    public Response create(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        subscriper.subscribe(webhook);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(SubscriberWebhooksExposure.class)
                .path(SubscriberWebhooksExposure.class, "get")
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
        Webhook webhook = subscriper.find(updated.getId()).orElseThrow(() -> throwNotFound(updated.getId().toString()));
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(webhook)
                .tag(w -> String.valueOf(w.getUpdated().toEpochSecond()))
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
        subscriper.unsubscribe(UUID.fromString(id));
        return Response.noContent().build();
    }

    @GET
    public Response list(@QueryParam("topics") final String topics) {
        return WebhookResponseBuilder
                .create()
                .entity(subscriper.list(WebhookEventTopics.parse(topics).getTopics()))
                .build();
    }

    @GET
    @Path("{id}")
    public Response get(@ValidUUID @NotNull @PathParam("id") final String id, @Context final Request request) {
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(subscriper
                        .find(UUID.fromString(id))
                        .orElseThrow(() -> throwNotFound(id)))
                .tag(w -> String.valueOf(w.getUpdated().toEpochSecond()))
                .build();
    }

    private WebhookException throwNotFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
