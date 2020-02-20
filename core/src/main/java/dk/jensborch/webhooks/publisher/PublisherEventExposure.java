package dk.jensborch.webhooks.publisher;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.ValidZonedDateTime;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEventTopics;
import dk.jensborch.webhooks.status.StatusRepository;

/**
 * Exposure for listing events published.
 */
@Path("/publisher-events")
@DeclareRoles("publisher")
@RolesAllowed("publisher")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherEventExposure {

    @Inject
    @Publisher
    StatusRepository repo;

    @GET
    public Response list(
            @QueryParam("topics") final String topics,
            @NotNull @ValidZonedDateTime @QueryParam("from") final String from,
            @Context final UriInfo uriInfo) {
        return Response.ok(repo.list(ZonedDateTime.parse(from), WebhookEventTopics.parse(topics).getTopics())).build();
    }

    @GET
    @Path("{id}")
    public Response get(
            @NotNull @PathParam("id") final UUID id) {
        return repo.find(id)
                .map(Response::ok)
                .orElse(notFound(id))
                .build();
    }

    private Response.ResponseBuilder notFound(final UUID id) {
        return Response.status(
                Response.Status.NOT_FOUND).entity(
                        new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + "not found"));
    }
}
