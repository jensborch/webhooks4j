package dk.jensborch.webhooks.publisher;

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
import dk.jensborch.webhooks.repositories.WebhookRepository;
import dk.jensborch.webhooks.validation.ValidUUID;

/**
 * Exposure for registration of webhooks.
 */
@Path("/publisher-webhooks")
@DeclareRoles({"subscriber", "publisher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherWebhookExposure {

    @Inject
    @Publisher
    WebhookRepository repo;

    @POST
    @RolesAllowed({"subscriber"})
    public Response create(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        if (webhook.getState() != Webhook.State.SUBSCRIBE) {
            throw new WebhookException(new WebhookError(WebhookError.Code.REGISTER_ERROR, "Illegal webhook status for " + webhook.getId()));
        }
        repo.save(webhook.state(Webhook.State.ACTIVE));
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(PublisherWebhookExposure.class)
                .path(PublisherWebhookExposure.class, "get")
                .build(webhook.getId()))
                .build();
    }

    @DELETE
    @RolesAllowed({"subscriber"})
    @Path("{id}")
    public Response delete(@NotNull @ValidUUID @PathParam("id") final String id) {
        repo.delete(UUID.fromString(id));
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({"subscriber", "publisher"})
    public Response list(@QueryParam("topics") final String topics) {
        return WebhookResponseBuilder
                .create()
                .entity(repo.list(WebhookEventTopics.parse(topics).getTopics()))
                .build();
    }

    @GET
    @RolesAllowed({"subscriber", "publisher"})
    @Path("{id}")
    public Response get(@NotNull @ValidUUID @PathParam("id") final String id, @Context final Request request) {
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(repo
                        .find(UUID.fromString(id))
                        .orElseThrow(() -> notFound(id)))
                .tag(w -> String.valueOf(w.getUpdated().toEpochSecond()))
                .build();
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
