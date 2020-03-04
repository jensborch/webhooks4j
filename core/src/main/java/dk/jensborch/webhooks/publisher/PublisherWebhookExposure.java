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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.validation.ValidUUID;
import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEventTopics;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookRepository;

/**
 * Exposure for registration of webhooks.
 */
@Path("/publisher-webhooks")
@DeclareRoles({"consumer", "publisher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherWebhookExposure {

    @Inject
    @Publisher
    WebhookRepository repo;

    @POST
    @RolesAllowed("consumer")
    public Response create(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        repo.save(webhook);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(PublisherWebhookExposure.class)
                .path(PublisherWebhookExposure.class, "get")
                .build(webhook.getId()))
                .build();
    }

    @GET
    @RolesAllowed({"consumer", "publisher"})
    public Response list(@QueryParam("topics") final String topics) {
        return Response.ok(repo.list(WebhookEventTopics.parse(topics).getTopics())).build();
    }

    @GET
    @RolesAllowed({"consumer", "publisher"})
    @Path("{id}")
    public Response get(@NotNull @ValidUUID @PathParam("id") final String id) {
        return repo.find(UUID.fromString(id))
                .map(Response::ok)
                .orElseThrow(() -> notFound(id))
                .build();
    }

    @DELETE
    @RolesAllowed("consumer")
    @Path("{id}")
    public Response delete(@NotNull @ValidUUID @PathParam("id") final String id) {
        repo.delete(UUID.fromString(id));
        return Response.noContent().build();
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
