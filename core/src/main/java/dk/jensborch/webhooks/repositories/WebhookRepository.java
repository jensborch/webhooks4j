package dk.jensborch.webhooks.repositories;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.jensborch.webhooks.Webhook;

/**
 * Repository for manipulating webhooks, used by both publisher and subscriber.
 */
public interface WebhookRepository {

    void save(@NotNull @Valid Webhook hook);

    void delete(@NotNull UUID id);

    Optional<Webhook> find(@NotNull UUID id);

    Set<Webhook> list(@NotNull String... topic);

    void touch(UUID id);

}
