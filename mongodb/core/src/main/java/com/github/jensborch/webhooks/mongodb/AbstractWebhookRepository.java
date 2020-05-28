package com.github.jensborch.webhooks.mongodb;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

/**
 * Abstract repository for webhooks.
 */
public abstract class AbstractWebhookRepository extends MongoRepository<Webhook> implements WebhookRepository {

    @Override
    public void save(final Webhook hook) {
        collection(Webhook.class).replaceOne(Filters.eq("_id", hook.getId()), hook, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete(final UUID id) {
        collection(Webhook.class).deleteOne(Filters.eq("_id", id));
    }

    @Override
    public Optional<Webhook> find(final UUID id) {
        return Optional.ofNullable(collection(Webhook.class)
                .find(Filters.and(Filters.eq("_id", id)))
                .limit(1)
                .first());
    }

    @Override
    public Set<Webhook> list(final String... topic) {
        BasicDBObject filter = new BasicDBObject();
        filter = topic.length > 0 ? new BasicDBObject("topics", new BasicDBObject("$elemMatch", new BasicDBObject("$in", topic))) : filter;
        return collection(Webhook.class).find(filter).into(new HashSet<>());
    }

    @Override
    public void touch(final UUID id) {
        find(id).ifPresent(w -> {
            ZonedDateTime max = statusRepository().firstFailed(id).map(WebhookEventStatus::getStart).orElse(null);
            collection(Webhook.class).replaceOne(Filters.eq("_id", w.getId()), w.touch(max));
        });
    }

    protected abstract AbstractStatusRepository statusRepository();

}
