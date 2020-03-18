package com.github.jensborch.webhooks.repositories;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.github.jensborch.webhooks.WebhookEventStatus;

/**
 * Repository for manipulating webhook event statuses.
 */
public interface WebhookEventStatusRepository {

    WebhookEventStatus save(@NotNull @Valid WebhookEventStatus status);

    Optional<WebhookEventStatus> find(@NotNull UUID eventId);

    SortedSet<WebhookEventStatus> list(@NotNull ZonedDateTime from, @NotNull String... topic);

    SortedSet<WebhookEventStatus> list(@NotNull ZonedDateTime from, @NotNull UUID webhook);

}
