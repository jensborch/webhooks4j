package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link Webhooks}.
 */
class WebhooksTest {

    @Test
    void testCreateEmpty() {
        Webhooks webhooks = new Webhooks();
        assertEquals(0, webhooks.getSize());
    }

    @Test
    void testCreate() {
        Webhooks webhooks = new Webhooks(Collections.singletonList(new Webhook()));
        assertEquals(1, webhooks.getSize());
    }

    @Test
    void testNull() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> new Webhooks(null));
        assertEquals("Webhooks should not be null", e.getMessage());
    }

}
