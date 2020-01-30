package dk.jensborch.webhooks.consumer;

import java.time.ZonedDateTime;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.status.StatusRepository;

/**
 * Exposure for receiving callback events.
 */
@Path("/consumer-events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsumerEventExposure {

    @Inject
    WebhookEventConsumer consumer;

    @Inject
    @Consumer
    StatusRepository repo;

    @POST
    public Response receive(
            final WebhookEvent callbackEvent,
            @Context final UriInfo uriInfo) {
        return Response.ok(consumer.consume(callbackEvent, uriInfo.getRequestUri())).build();
    }

    @GET
    public Response list(
            @QueryParam("topics") final String topics,
            @NotNull @QueryParam("from") final ZonedDateTime from,
            @Context final UriInfo uriInfo) {
        String[] t = topics == null ? new String[]{} : topics.split(",");
        return Response.ok(repo.list(from, t)).build();
    }
}
