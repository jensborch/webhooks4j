package com.github.jensborch.webhooks.publisher;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookDocumentation;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;
import com.github.jensborch.webhooks.WebhookEventTopics;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseBuilder;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.github.jensborch.webhooks.validation.ValidUUID;
import com.github.jensborch.webhooks.validation.ValidZonedDateTime;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
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
                content = @Content(
                        schema = @Schema(implementation = WebhookEventStatuses.class)
                )
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public Response list(
            @QueryParam("topics") final String topics,
            @ValidUUID @QueryParam("webhook") final String webhook,
            @QueryParam("status") final String status,
            @NotNull @ValidZonedDateTime @QueryParam("from") final String from,
            @Context final UriInfo uriInfo) {
        LOG.debug("Listing events using webhook {}, topics {}, from {} and state {}", webhook, topics, from, status);
        if (webhook == null) {
            return WebhookResponseBuilder
                    .create()
                    .entity(repo.list(ZonedDateTime.parse(from), WebhookEventStatus.Status.fromString(status), WebhookEventTopics.parse(topics).getTopics()))
                    .build();
        } else {
            return WebhookResponseBuilder
                    .create()
                    .entity(repo.list(ZonedDateTime.parse(from), WebhookEventStatus.Status.fromString(status), UUID.fromString(webhook)))
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
                .entity(found)
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
