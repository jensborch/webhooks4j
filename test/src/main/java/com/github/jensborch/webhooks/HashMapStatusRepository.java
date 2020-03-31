package com.github.jensborch.webhooks;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;

/**
 * HashMap based {@link WebhookEventStatusRepository} implementation.
 */
public abstract class HashMapStatusRepository implements WebhookEventStatusRepository {

    private final ConcurrentHashMap<UUID, WebhookEventStatus> map = new ConcurrentHashMap<>();

    @Override
    public WebhookEventStatus save(final WebhookEventStatus status) {
        map.put(status.getId(), status);
        return status;
    }

    @Override
    public Optional<WebhookEventStatus> find(final UUID id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public SortedSet<WebhookEventStatus> list(final ZonedDateTime from, final String... topic) {
        return map.values().stream()
                .filter(p -> p.getStart().isAfter(from))
                .filter(p -> topic == null || topic.length == 0 || Arrays.binarySearch(topic, p.getEvent().getTopic()) >= 0)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<WebhookEventStatus> list(final ZonedDateTime from, final UUID webhook) {
        return map.values().stream()
                .filter(p -> p.getStart().isAfter(from))
                .filter(p -> p.getEvent().getWebhook().map(w -> w.equals(webhook)).orElse(false))
                .collect(Collectors.toCollection(TreeSet::new));
    }

}
