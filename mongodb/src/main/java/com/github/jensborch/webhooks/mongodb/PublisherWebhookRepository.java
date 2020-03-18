package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.client.MongoCollection;
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.publisher.Publisher;

/**
 * Repository for published webhooks.
 */
@Publisher
@ApplicationScoped
public class PublisherWebhookRepository extends AbstractWebhookRepository {

    @Inject
    @Publisher
    private MongoCollection<Webhook> collection;

    @Override
    protected MongoCollection<Webhook> collection() {
        return collection;
    }
}
