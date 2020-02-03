package dk.jensborch.webhooks.repository;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
public interface WebhookRepository {

    void save(@NotNull @Valid Webhook hook);

    void delte(@NotNull UUID id);

    Optional<Webhook> find(@NotNull UUID id);

    Optional<Webhook> findByPublisher(@NotNull URI publisher);

    Set<Webhook> list(@NotNull @Size(min = 1) String... topic);

}
