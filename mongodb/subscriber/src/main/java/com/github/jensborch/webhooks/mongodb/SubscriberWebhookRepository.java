package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.jensborch.webhooks.subscriber.Subscriber;
import com.mongodb.client.MongoDatabase;

/**
 * Repository for consumed webhooks.
 */
@Subscriber
@ApplicationScoped
public class SubscriberWebhookRepository extends AbstractWebhookRepository {

    @Inject
    @Subscriber
    private MongoDatabase db;

    @Inject
    @Subscriber
    private SubscriberStatusRepository statusRepo;

    @Override
    protected String collectionName() {
        return "SubscriberWebhooks";
    }

    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    protected MongoDatabase db() {
        return db;
    }

    @Override
    protected AbstractStatusRepository statusRepository() {
        return statusRepo;
    }


}
