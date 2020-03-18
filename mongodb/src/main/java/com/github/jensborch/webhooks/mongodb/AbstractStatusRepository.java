package com.github.jensborch.webhooks.mongodb;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.conversions.Bson;

/**
 * Abstract repository for webhooks statuses.
 */
public abstract class AbstractStatusRepository implements WebhookEventStatusRepository {

    @Override
    public WebhookEventStatus save(@NotNull @Valid final WebhookEventStatus status) {
        collection().replaceOne(Filters.eq("_id", status.getId()), status, new ReplaceOptions().upsert(true));
        return status;
    }

    @Override
    public Optional<WebhookEventStatus> find(@NotNull final UUID eventId) {
        return Optional.of(collection())
                .map(hooks -> hooks.find(Filters.eq("_id", eventId)))
                .map(MongoIterable::first);
    }

    @Override
    public SortedSet<WebhookEventStatus> list(@NotNull final ZonedDateTime from, final String... topic) {
        Bson filter = Filters.gt("start", from);
        filter = topic.length > 0 ? Filters.and(filter, Filters.in("event.topic", topic)) : filter;
        return collection()
                .find(filter)
                .into(new TreeSet<>());
    }

    @Override
    public SortedSet<WebhookEventStatus> list(final ZonedDateTime from, final UUID webhook) {
        return collection()
                .find(Filters.eq("webhook", webhook))
                .into(new TreeSet<>());
    }

    protected abstract MongoCollection<WebhookEventStatus> collection();
}
