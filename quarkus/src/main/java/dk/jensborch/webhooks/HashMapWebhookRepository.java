package dk.jensborch.webhooks;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dk.jensborch.webhooks.publisher.WebhookRepository;

/**
 *
 */
public abstract class HashMapWebhookRepository implements WebhookRepository {

    protected final ConcurrentHashMap<UUID, Webhook> map = new ConcurrentHashMap<>();

    @Override
    public Set<Webhook> find(final String topic) {
        return map.entrySet().stream().filter((e) -> e.getValue().getTopics().contains(topic)).map((e) -> e.getValue()).collect(Collectors.toSet());
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
    public Webhook get(final UUID id) {
        return map.get(id);
    }

}
