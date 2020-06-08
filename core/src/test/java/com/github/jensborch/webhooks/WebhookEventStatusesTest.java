package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookEventStatuses}.
 */
class WebhookEventStatusesTest {

    @Test
    void testCreateEmpty() {
        WebhookEventStatuses statuses = new WebhookEventStatuses();
        assertEquals(0, statuses.getSize());
    }

    @Test
    void testCreate() {
        WebhookEventStatuses statuses = new WebhookEventStatuses(Collections.singletonList(new WebhookEventStatus()));
        assertEquals(1, statuses.getSize());
    }

    @Test
    void testNull() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new WebhookEventStatuses(null));
        assertEquals("Statuses should not be null", e.getMessage());
    }

}
