package com.github.jensborch.webhooks.mongodb;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.PostConstruct;

import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * Abstract repository for webhooks statuses.
 */
public abstract class AbstractStatusRepository extends MongoRepository<WebhookEventStatus> implements WebhookEventStatusRepository {

    @PostConstruct
    public void init() {
        collection(WebhookEventStatus.class).createIndex(new Document("event.webhook", 1));
    }

    @Override
    public WebhookEventStatus save(final WebhookEventStatus status) {
        collection(WebhookEventStatus.class).replaceOne(Filters.eq("_id", status.getId()), status, new ReplaceOptions().upsert(true));
        return status;
    }

    @Override
    public Optional<WebhookEventStatus> find(final UUID eventId) {
        return Optional.ofNullable(collection(WebhookEventStatus.class)
                .find(Filters.and(Filters.eq("_id", eventId)))
                .limit(1)
                .first());
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
                .find(Filters.and(Filters.eq("event.webhook", webhook), Filters.gt("start", from)))
                .into(new TreeSet<>());
    }

    public Optional<WebhookEventStatus> firstFailed(final UUID webhook) {
        return Optional.ofNullable(collection(WebhookEventStatus.class)
                .find(Filters.and(Filters.eq("event.webhook", webhook), Filters.eq("status", WebhookEventStatus.Status.FAILED)))
                .sort(new BasicDBObject("end", 1))
                .limit(1)
                .first());

    }

}
