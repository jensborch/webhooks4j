package dk.jensborch.webhooks.publisher;

import java.util.UUID;

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

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.repository.WebhookRepository;

/**
 *
 */
@Path("/publisher-webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherWebhookExposure {

    @Inject
    @Publisher
    WebhookRepository repo;

    @POST
    public Response create(@NotNull @Valid final Webhook webhook,
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
    public Response list(@QueryParam("topic") final String topics) {
        String[] t = topics == null ? new String[]{} : topics.split(",");
        return Response.ok(repo.list(t)).build();
    }

    @GET
    @Path("{id}")
    public Response get(@NotNull @PathParam("id") final UUID id) {
        return Response.ok(repo.find(id)).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@NotNull @PathParam("id") final String id) {
        repo.delte(UUID.fromString(id));
        return Response.noContent().build();
    }

}
