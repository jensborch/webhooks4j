package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link PublisherStatusRepository}.
 */
class PublisherStatusRepositoryTest {

    @Test
    void testCollectionName() {
        PublisherStatusRepository instance = new PublisherStatusRepository();
        assertEquals("PublisherStatuses", instance.collectionName());
    }

}
