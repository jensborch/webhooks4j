package com.github.jensborch.webhooks.publisher;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventTopics;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseBuilder;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.validation.ValidUUID;
import com.github.jensborch.webhooks.validation.ValidZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposure for listing events published.
 */
@Path(Webhook.PublisherEndpoints.EVENTS_PATH)
@DeclareRoles({"subscriber", "publisher"})
@RolesAllowed({"subscriber", "publisher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PublisherEventExposure {

    private static final Logger LOG = LoggerFactory.getLogger(PublisherEventExposure.class);

    @Inject
    @Publisher
    WebhookEventStatusRepository repo;

    @GET
    public Response list(
            @QueryParam("topics") final String topics,
            @ValidUUID @QueryParam("webhook") final String webhook,
            @NotNull @ValidZonedDateTime @QueryParam("from") final String from,
            @Context final UriInfo uriInfo) {
        LOG.debug("Listing events using webhook {}, topics {} and from {}", webhook, topics, from);
        if (webhook == null) {
            return WebhookResponseBuilder
                    .create()
                    .entity(repo.list(ZonedDateTime.parse(from), WebhookEventTopics.parse(topics).getTopics()))
                    .build();
        } else {
            return WebhookResponseBuilder
                    .create()
                    .entity(repo.list(ZonedDateTime.parse(from), UUID.fromString(webhook)))
                    .build();
        }
    }

    @GET
    @Path("{id}")
    public Response get(
            @NotNull @ValidUUID @PathParam("id") final String id,
            @Context final Request request) {
        return WebhookResponseBuilder
                .create(request, WebhookEventStatus.class)
                .tag(e -> String.valueOf(e.getStart().toInstant().toEpochMilli()))
                .entity(repo
                        .find(UUID.fromString(id))
                        .orElseThrow(() -> notFound(id)))
                .build();
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }
}
