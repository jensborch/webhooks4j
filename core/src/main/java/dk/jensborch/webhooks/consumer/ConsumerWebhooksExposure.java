package dk.jensborch.webhooks.consumer;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEventTopics;

/**
 * Exposure for registration of webhooks.
 */
@Path("/consumer-webhooks")
@DeclareRoles("consumer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsumerWebhooksExposure {

    @Inject
    WebhookRegistry registry;

    @GET
    @RolesAllowed("consumer")
    public Response list(@QueryParam("topics") final String topics) {
        return Response.ok(registry.list(WebhookEventTopics.parse(topics).getTopics())).build();
    }

    @GET
    @Path("{id}")
    @RolesAllowed("consumer")
    public Response get(@NotNull @PathParam("id") final UUID id) {
        return registry.find(id)
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
