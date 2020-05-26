package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SubscriberStatusRepository}.
 */
public class SubscriberStatusRepositoryTest {

    @Test
    public void testCollectionName() {
        SubscriberStatusRepository instance = new SubscriberStatusRepository();
        assertEquals("SubscriberStatuses", instance.collectionName());
    }

}
