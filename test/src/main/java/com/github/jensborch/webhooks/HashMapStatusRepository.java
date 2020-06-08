package com.github.jensborch.webhooks;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
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
    public WebhookEventStatuses list(final ZonedDateTime from, final WebhookEventStatus.Status status, final String... topic) {
        return new WebhookEventStatuses(map.values().stream()
                .filter(p -> p.getStart().isAfter(from))
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> topic == null || topic.length == 0 || Arrays.binarySearch(topic, p.getEvent().getTopic()) >= 0)
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    @Override
    public WebhookEventStatuses list(final ZonedDateTime from, final WebhookEventStatus.Status status, final UUID webhook) {
        return new WebhookEventStatuses(map.values().stream()
                .filter(p -> p.getStart().isAfter(from))
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> Optional.ofNullable(p.getEvent().getWebhook()).map(w -> w.equals(webhook)).orElse(false))
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    public Optional<WebhookEventStatus> lastFailed() {
        return map.values().stream()
                .filter(s -> s.getStatus() == WebhookEventStatus.Status.FAILED)
                .sorted(Comparator.reverseOrder())
                .findFirst();
    }
}
