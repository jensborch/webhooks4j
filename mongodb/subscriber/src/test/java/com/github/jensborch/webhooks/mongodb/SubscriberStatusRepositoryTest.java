package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SubscriberStatusRepository}.
 */
class SubscriberStatusRepositoryTest {

    @Test
    void testCollectionName() {
        SubscriberStatusRepository instance = new SubscriberStatusRepository();
        assertEquals("SubscriberStatuses", instance.collectionName());
    }

}
