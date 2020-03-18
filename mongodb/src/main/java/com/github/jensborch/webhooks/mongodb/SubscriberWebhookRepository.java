package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.client.MongoCollection;
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.subscriber.Subscriber;

/**
 * Repository for consumed webhooks.
 */
@Subscriber
@ApplicationScoped
public class SubscriberWebhookRepository extends AbstractWebhookRepository {

    @Inject
    @Subscriber
    private MongoCollection<Webhook> collection;

    @Override
    protected MongoCollection<Webhook> collection() {
        return collection;
    }

}
