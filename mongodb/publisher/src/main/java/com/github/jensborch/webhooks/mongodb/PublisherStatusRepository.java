package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.jensborch.webhooks.publisher.Publisher;
import com.mongodb.client.MongoDatabase;

/**
 * Repository for published webhooks status.
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends AbstractStatusRepository {

    @Inject
    @Publisher
    private MongoDatabase db;

    @Override
    protected String collectionName() {
        return "PublisherStatuses";
    }

    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    protected MongoDatabase db() {
        return db;
    }
}
