package dk.jensborch.webhooks.consumer;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
@Path("/consumer-webhooks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WebhooksExposure {

    @Inject
    WebhookRegistry registry;

    @POST
    public Response create(
            final Webhook webhook,
            @Context final UriInfo uriInfo) {
        registry.registre(webhook);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(WebhooksExposure.class)
                .path("get")
                .build(webhook.getId()))
                .build();
    }

    @POST
    @Path("{id}")
    public Response get(@PathParam("id") final UUID id) {
        return Response.ok(registry.get(id)).build();
    }

}
