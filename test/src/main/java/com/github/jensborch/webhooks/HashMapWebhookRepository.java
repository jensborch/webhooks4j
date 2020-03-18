package com.github.jensborch.webhooks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jensborch.webhooks.repositories.WebhookRepository;

/**
 * Test implementation of a WebhookRepository.
 */
public abstract class HashMapWebhookRepository implements WebhookRepository {

    protected final ConcurrentHashMap<UUID, Webhook> map = new ConcurrentHashMap<>();

    @Override
    public Set<Webhook> list(final String... topic) {
        if (topic.length > 0) {
            return map.values().stream()
                    .filter(w -> Arrays.stream(topic).anyMatch(t -> w.getTopics().contains(t)))
                    .collect(Collectors.toSet());
        } else {
            return new HashSet<>(map.values());
        }
    }

    @Override
    public void save(final Webhook hook) {
        map.put(hook.getId(), hook);
    }

    @Override
    public void delete(final UUID id) {
        map.remove(id);
    }

    @Override
    public Optional<Webhook> find(final UUID id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public void touch(final UUID id) {
        map.get(id).touch();
    }

}
