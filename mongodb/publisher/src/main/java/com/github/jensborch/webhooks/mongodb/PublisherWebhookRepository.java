package com.github.jensborch.webhooks.mongodb;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.github.jensborch.webhooks.publisher.Publisher;
import com.mongodb.client.MongoDatabase;

/**
 * Repository for published webhooks.
 */
@Publisher
@ApplicationScoped
public class PublisherWebhookRepository extends AbstractWebhookRepository {

    @Inject
    @Publisher
    private MongoDatabase db;

    @Inject
    @Publisher
    private PublisherStatusRepository statusRepo;

    @Override
    protected String collectionName() {
        return "PublisherWebhooks";
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
