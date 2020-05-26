package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.jensborch.webhooks.subscriber.Subscriber;
import com.mongodb.client.MongoDatabase;

/**
 * Repository for consumed webhooks status.
 */
@Subscriber
@ApplicationScoped
public class SubscriberStatusRepository extends AbstractStatusRepository {

    @Inject
    @Subscriber
    private MongoDatabase db;

    @Override
    protected String collectionName() {
        return "SubscriberStatusRepository";
    }

    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    protected MongoDatabase db() {
        return db;
    }

}
