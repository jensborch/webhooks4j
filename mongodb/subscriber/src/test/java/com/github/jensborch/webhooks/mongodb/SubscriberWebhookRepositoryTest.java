package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SubscriberWebhookRepository}.
 */
class SubscriberWebhookRepositoryTest {

    @Test
    void testCollectionName() {
        SubscriberWebhookRepository instance = new SubscriberWebhookRepository();
        assertEquals("SubscriberWebhooks", instance.collectionName());
    }

}
