package com.github.jensborch.webhooks.publisher;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
import com.github.jensborch.webhooks.WebhookEventTopics;
import com.github.jensborch.webhooks.WebhookException;
import com.github.jensborch.webhooks.WebhookResponseBuilder;
import com.github.jensborch.webhooks.Webhooks;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import com.github.jensborch.webhooks.validation.ValidUUID;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposure for registration of webhooks.
 */
@Path(Webhook.PublisherEndpoints.WEBHOOKS_PATH)
@DeclareRoles({"subscriber", "publisher"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@SuppressWarnings("PMD.ExcessiveImports")
public class PublisherWebhookExposure {

    private static final Logger LOG = LoggerFactory.getLogger(PublisherWebhookExposure.class);

    @Inject
    @Publisher
    WebhookRepository repo;

    @POST
    @RolesAllowed({"subscriber"})
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.SUBSCRIBED,
                responseCode = "201"
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
    public Response subscribe(
            @NotNull @Valid final Webhook webhook,
            @Context final UriInfo uriInfo) {
        if (webhook.getState() != Webhook.State.SUBSCRIBE) {
            throw new WebhookException(new WebhookError(WebhookError.Code.VALIDATION_ERROR, "Illegal webhook status for " + webhook.getId()));
        }
        LOG.debug("Subscribing to webhook {}", webhook);
        repo.save(webhook.state(Webhook.State.ACTIVE));
        return Response.created(uriInfo
                .getBaseUriBuilder()
                .path(PublisherWebhookExposure.class)
                .path(PublisherWebhookExposure.class, "get")
                .build(webhook.getId()))
                .build();
    }

    @DELETE
    @RolesAllowed({"subscriber"})
    @Path("{id}")
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.DELETED,
                responseCode = "202"
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
    public Response delete(@NotNull @ValidUUID @PathParam("id") final String id) {
        repo.delete(UUID.fromString(id));
        return Response.noContent().build();
    }

    @GET
    @RolesAllowed({"subscriber", "publisher"})
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.WEBHOOK,
                responseCode = "200",
                content = @Content(schema = @Schema(implementation = Webhooks.class))
        ),
        @ApiResponse(
                description = WebhookDocumentation.VALIDATION_ERROR,
                responseCode = "400",
                content = @Content(
                        schema = @Schema(implementation = WebhookError.class)
                )
        )
    })
    public Response list(@QueryParam("topics") final String topics) {
        return WebhookResponseBuilder
                .create()
                .entity(new Webhooks(repo.list(WebhookEventTopics.parse(topics).getTopics())))
                .build();
    }

    @GET
    @RolesAllowed({"subscriber", "publisher"})
    @Path("{id}")
    @ApiResponses(value = {
        @ApiResponse(
                description = WebhookDocumentation.WEBHOOK,
                responseCode = "200",
                content = @Content(
                        schema = @Schema(implementation = Webhook.class)
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
    public Response get(@NotNull @ValidUUID @PathParam("id") final String id, @Context final Request request) {
        return WebhookResponseBuilder
                .create(request, Webhook.class)
                .entity(repo
                        .find(UUID.fromString(id))
                        .orElseThrow(() -> notFound(id)))
                .tag(w -> String.valueOf(w.getUpdated().toInstant().toEpochMilli()))
                .build();
    }

    private WebhookException notFound(final String id) {
        return new WebhookException(new WebhookError(WebhookError.Code.NOT_FOUND, "Webhook " + id + " not found"));
    }

}
