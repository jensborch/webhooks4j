package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.client.MongoCollection;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.publisher.Publisher;

/**
 * Repository for published webhooks status.
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends AbstractStatusRepository {

    @Inject
    @Publisher
    private MongoCollection<WebhookEventStatus> collection;

    @Override
    protected MongoCollection<WebhookEventStatus> collection() {
        return collection;
    }
}
