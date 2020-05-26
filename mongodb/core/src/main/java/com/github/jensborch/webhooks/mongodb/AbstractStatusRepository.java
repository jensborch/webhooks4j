package com.github.jensborch.webhooks.mongodb;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.conversions.Bson;

/**
 * Abstract repository for webhooks statuses.
 */
public abstract class AbstractStatusRepository extends MongoRepository<WebhookEventStatus> implements WebhookEventStatusRepository {

    @Override
    public WebhookEventStatus save(final WebhookEventStatus status) {
        collection(WebhookEventStatus.class).replaceOne(Filters.eq("_id", status.getId()), status, new ReplaceOptions().upsert(true));
        return status;
    }

    @Override
    public Optional<WebhookEventStatus> find(final UUID eventId) {
        return Optional.of(collection(WebhookEventStatus.class))
                .map(hooks -> hooks.find(Filters.eq("_id", eventId)))
                .map(MongoIterable::first);
    }

    @Override
    public SortedSet<WebhookEventStatus> list(final ZonedDateTime from, final String... topic) {
        Bson filter = Filters.gt("start", from);
        filter = topic.length > 0 ? Filters.and(filter, Filters.in("event.topic", topic)) : filter;
        return collection(WebhookEventStatus.class)
                .find(filter)
                .into(new TreeSet<>());
    }

    @Override
    public SortedSet<WebhookEventStatus> list(final ZonedDateTime from, final UUID webhook) {
        return collection(WebhookEventStatus.class)
                .find(Filters.eq("webhook", webhook))
                .into(new TreeSet<>());
    }

    public WebhookEventStatus firstFailed(final UUID webhook) {
        return collection(WebhookEventStatus.class)
                .find(Filters.and(Filters.eq("webhook", webhook), Filters.eq("status", WebhookEventStatus.Status.FAILED)))
                .sort(new BasicDBObject("end", 1))
                .limit(1).first();

    }

}
