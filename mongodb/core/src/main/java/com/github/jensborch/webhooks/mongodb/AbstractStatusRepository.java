package com.github.jensborch.webhooks.mongodb;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import jakarta.inject.Inject;

import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.WebhookEventStatuses;
import com.github.jensborch.webhooks.WebhookTTLConfiguration;
import com.github.jensborch.webhooks.repositories.WebhookEventStatusRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract repository for webhooks statuses.
 */
public abstract class AbstractStatusRepository extends MongoRepository<WebhookEventStatus> implements WebhookEventStatusRepository {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractStatusRepository.class);

    @Inject
    WebhookTTLConfiguration conf;

    @PostConstruct
    public void init() {
        collection(WebhookEventStatus.class).createIndex(Indexes.ascending("event.webhook"));
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating TTL index using {} and {}", conf.getAmount(), conf.getUnit());
        }
        collection(WebhookEventStatus.class).createIndex(new Document("end", 1), new IndexOptions().name("ttl").expireAfter(conf.getAmount(), conf.getUnit()));
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
    public WebhookEventStatuses list(final ZonedDateTime from, final WebhookEventStatus.Status status, final String... topic) {
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(Filters.gt("start", from));
        if (topic.length > 0) {
            filters.add(Filters.in("event.topic", topic));
        }
        if (status != null) {
            filters.add(Filters.eq("status", status.toString()));
        }
        return new WebhookEventStatuses(collection(WebhookEventStatus.class)
                .find(Filters.and(filters.toArray(new Bson[0])))
                .into(new TreeSet<>()));
    }

    @Override
    public WebhookEventStatuses list(final ZonedDateTime from, final WebhookEventStatus.Status status, final UUID webhook) {
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq("event.webhook", webhook));
        filters.add(Filters.gt("start", from));
        if (status != null) {
            filters.add(Filters.eq("status", status.toString()));
        }
        return new WebhookEventStatuses(collection(WebhookEventStatus.class)
                .find(Filters.and(filters.toArray(new Bson[0])))
                .into(new TreeSet<>()));
    }

    public Optional<WebhookEventStatus> firstFailed(final UUID webhook) {
        return Optional.ofNullable(collection(WebhookEventStatus.class)
                .find(Filters.and(Filters.eq("event.webhook", webhook), Filters.eq("status", WebhookEventStatus.Status.FAILED.toString())))
                .sort(new BasicDBObject("end", -1))
                .limit(1)
                .first());

    }

}
