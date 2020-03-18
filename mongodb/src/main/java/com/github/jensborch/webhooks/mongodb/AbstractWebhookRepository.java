package com.github.jensborch.webhooks.mongodb;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.repositories.WebhookRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

/**
 * Abstract repository for webhooks.
 */
public abstract class AbstractWebhookRepository implements WebhookRepository {

    @Override
    public void save(@NotNull @Valid final Webhook hook) {
        collection().replaceOne(Filters.eq("_id", hook.getId()), hook, new ReplaceOptions().upsert(true));
    }

    @Override
    public void delete(@NotNull final UUID id) {
        collection().deleteOne(Filters.eq("_id", id));
    }

    @Override
    public Optional<Webhook> find(@NotNull final UUID id) {
        return Optional.of(collection())
            .map(hooks -> hooks.find(Filters.eq("_id", id)))
            .map(MongoIterable::first);
    }

    @Override
    public Set<Webhook> list(final String... topic) {
        BasicDBObject filter = new BasicDBObject();
        filter = topic.length > 0 ? new BasicDBObject("topics", new BasicDBObject("$elemMatch", new BasicDBObject("$in", topic))) : filter;
        return collection().find(filter).into(new HashSet<>());
    }

    @Override
    public void touch(final UUID id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected abstract MongoCollection<Webhook> collection();
}
