package com.github.jensborch.webhooks.subscriber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import com.github.jensborch.webhooks.Webhook;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookSyncConfiguration}.
 */
class WebhookSyncConfigurationTest {

    @Test
    void testSyncFrom() throws Exception {
        WebhookSyncConfiguration configuration = new WebhookSyncConfiguration() {

            @Override
            public long getSyncOffset() {
                return 1;
            }

            @Override
            public TemporalUnit getSyncOffsetUnit() {
                return ChronoUnit.SECONDS;
            }
        };
        ZonedDateTime now = ZonedDateTime.now();
        Webhook webhook = new Webhook(new URI("http://pub.dk"), new URI("http://sub.dk"), "test").updated(now);
        assertEquals(now.minusSeconds(1), configuration.syncFrom(webhook));
    }

}
