package com.github.jensborch.webhooks.publisher;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import com.github.jensborch.webhooks.WebhookDocumentation;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventTopics;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseBuilder;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.validation.ValidUUID;
import com.github.jensborch.webhooks.validation.ValidZonedDateTime;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@SuppressWarnings("PMD.ExcessiveImports")
public class PublisherEventExposure {

    private static final Logger LOG = LoggerFactory.getLogger(PublisherEventExposure.class);

    @Inject
    @Publisher
    WebhookEventStatusRepository repo;

    @GET
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.EVENT_STATUS,
                responseCode = "200",
                content = @Content(array = @ArraySchema(
                        schema = @Schema(implementation = WebhookEventStatus.class)
                ))
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
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
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.EVENT_STATUS,
                responseCode = "200",
                content = @Content(
                        schema = @Schema(implementation = WebhookEventStatus.class)
                )
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        ),
        @ApiResponse(
                description = WebhookDocumentation.NOT_FOUND,
                responseCode = "404",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
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

    @PUT
    @Path("{id}")
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.EVENT_STATUS,
                responseCode = "200",
                content = @Content(
                        schema = @Schema(implementation = WebhookEventStatus.class)
                )
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        ),
        @ApiResponse(
                description = WebhookDocumentation.NOT_FOUND,
                responseCode = "404",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
    public Response update(
            @NotNull @ValidUUID @PathParam("id") final String id,
            @NotNull @Valid final WebhookEventStatus status,
            @Context final Request request) {
        requireStatusSuccess(status);
        requireSameId(id, status);
        WebhookEventStatus found = repo
                .find(UUID.fromString(id))
                .orElseThrow(() -> notFound(id));
        return WebhookResponseBuilder
                .create(request, WebhookEventStatus.class)
                .entity(status)
                .tag(e -> String.valueOf(e.getStart().toInstant().toEpochMilli()))
                .fulfilled(s -> Response.ok(repo.save(found.done(true))))
                .build();
    }

    private void requireStatusSuccess(final WebhookEventStatus status) {
        if (status.getStatus() != WebhookEventStatus.Status.SUCCESS) {
            throw new WebhookException(new WebhookError(WebhookError.Code.VALIDATION_ERROR, "Illegal event status for " + status.getId() + " - status must be SUCCESS"));
        }
    }

    private void requireSameId(final String id, final WebhookEventStatus status) {
        if (!id.equals(status.getId().toString())) {
            throw new WebhookException(new WebhookError(WebhookError.Code.VALIDATION_ERROR, "Illegal event id for " + status.getId() + " - id must equal " + id));
        }
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }
}
