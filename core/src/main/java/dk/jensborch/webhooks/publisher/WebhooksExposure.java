package dk.jensborch.webhooks.publisher;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
@Path("/webhooks")
public class WebhooksExposure {

    @Inject
    private WebhookRepository repo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(final Webhook hook) {
        repo.save(hook);
        return Response.accepted().build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final String id) {
        repo.delte(UUID.fromString(id));
        return Response.noContent().build();
    }

}
