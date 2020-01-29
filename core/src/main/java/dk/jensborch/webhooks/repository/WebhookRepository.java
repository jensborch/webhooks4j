package dk.jensborch.webhooks.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.Dependent;

import dk.jensborch.webhooks.Webhook;

/**
 *
 */
@Dependent
public interface WebhookRepository {

    void save(Webhook hook);

    void delte(UUID id);

    Optional<Webhook> find(UUID id);

    Set<Webhook> list(String topic);

}
