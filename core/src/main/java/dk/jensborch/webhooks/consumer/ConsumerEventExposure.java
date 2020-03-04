package dk.jensborch.webhooks.consumer;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import dk.jensborch.webhooks.validation.ValidZonedDateTime;
import dk.jensborch.webhooks.WebhookError;
import dk.jensborch.webhooks.WebhookEvent;
import dk.jensborch.webhooks.WebhookEventTopics;
import dk.jensborch.webhooks.WebhookException;
import dk.jensborch.webhooks.repositories.WebhookEventStatusRepository;

/**
 * Exposure for receiving callback events.
 */
@Path("/consumer-events")
@DeclareRoles({"consumer", "publisher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsumerEventExposure {

    @Inject
    WebhookEventConsumer consumer;

    @Inject
    @Consumer
    WebhookEventStatusRepository repo;

    @POST
    @RolesAllowed("publisher")
    public Response receive(
            @NotNull @Valid final WebhookEvent callbackEvent,
            @Context final UriInfo uriInfo) {
        consumer.consume(callbackEvent);
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(ConsumerEventExposure.class)
                .path(ConsumerEventExposure.class, "get")
                .build(callbackEvent.getId()))
                .build();
    }

    @GET
    @RolesAllowed({"consumer", "publisher"})
    public Response list(
            @QueryParam("topics") final String topics,
            @ValidUUID @QueryParam("webhook") final String webhook,
            @NotNull @ValidZonedDateTime @QueryParam("from") final String from,
            @Context final UriInfo uriInfo) {
        if (webhook == null) {
            return Response
                    .ok(repo.list(ZonedDateTime.parse(from), WebhookEventTopics.parse(topics).getTopics()))
                    .build();
        } else {
            return Response
                    .ok(repo.list(ZonedDateTime.parse(from), UUID.fromString(webhook)))
                    .build();
        }
    }

    @GET
    @Path("{id}")
    @RolesAllowed({"consumer", "publisher"})
    public Response get(
            @NotNull @ValidUUID @PathParam("id") final String id) {
        return repo.find(UUID.fromString(id))
                .map(Response::ok)
                .orElseThrow(() -> notFound(id))
                .build();
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
