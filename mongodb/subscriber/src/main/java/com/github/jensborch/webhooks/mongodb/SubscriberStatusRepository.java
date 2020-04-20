package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.client.MongoCollection;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.github.jensborch.webhooks.subscriber.Subscriber;

/**
 * Repository for consumed webhooks status.
 */
@Subscriber
@ApplicationScoped
public class SubscriberStatusRepository extends AbstractStatusRepository {

    @Inject
    @Subscriber
    private MongoCollection<WebhookEventStatus> collection;

    @Override
    protected MongoCollection<WebhookEventStatus> collection() {
        return collection;
    }

}
