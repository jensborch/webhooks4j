package dk.jensborch.webhooks;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dk.jensborch.webhooks.repository.WebhookRepository;

/**
 *
 */
public abstract class HashMapWebhookRepository implements WebhookRepository {

    protected final ConcurrentHashMap<UUID, Webhook> map = new ConcurrentHashMap<>();

    @Override
    public Set<Webhook> list(final String topic) {
        return map.values().stream()
                .filter(w -> w.getTopics().contains(topic))
                .collect(Collectors.toSet());
    }

    @Override
    public void save(final Webhook hook) {
        map.put(hook.getId(), hook);
    }

    @Override
    public void delte(final UUID id) {
        map.remove(id);
    }

    @Override
    public Optional<Webhook> find(final UUID id) {
        return Optional.ofNullable(map.get(id));
    }

}
