package com.github.jensborch.webhooks.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;

/**
 * Repository for manipulating webhook event statuses.
 */
public interface WebhookEventStatusRepository {

    WebhookEventStatus save(@NotNull @Valid WebhookEventStatus status);

    Optional<WebhookEventStatus> find(@NotNull UUID eventId);

    WebhookEventStatuses list(@NotNull ZonedDateTime from, WebhookEventStatus.Status status, @NotNull String... topic);

    WebhookEventStatuses list(@NotNull ZonedDateTime from, WebhookEventStatus.Status status, @NotNull UUID webhook);

}
