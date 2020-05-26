package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link PublisherWebhookRepository}.
 */
class PublisherWebhookRepositoryTest {

    @Test
    void testCollectionName() {
        PublisherWebhookRepository instance = new PublisherWebhookRepository();
        assertEquals("PublisherWebhooks", instance.collectionName());
    }

}
