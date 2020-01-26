package dk.jensborch.webhooks.consumer;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.WebhookEvent;

/**
 * Exposure for receiving callback events.
 */
@Path("/consumer-events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CallbackExposure {

    @Inject
    WebhookEventConsumer consumer;

    @POST
    public Response receive(
            final WebhookEvent callbackEvent,
            @Context final UriInfo uriInfo) {
        return Response.ok(consumer.consume(callbackEvent, uriInfo.getRequestUri())).build();
    }
}
