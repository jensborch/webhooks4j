package com.github.jensborch.webhooks.repositories;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.github.jensborch.webhooks.Webhook;

/**
 * Repository for manipulating webhooks, used by both publisher and subscriber.
 */
public interface WebhookRepository {

    /**
     * Saves new or updates existing webhook in the repository.
       *
     * @param webhook saved to repository
     */
    void save(@NotNull @Valid Webhook webhook);

    void delete(@NotNull UUID id);

    Optional<Webhook> find(@NotNull UUID id);

    Set<Webhook> list(@NotNull String... topic);

    void touch(UUID id);

}
