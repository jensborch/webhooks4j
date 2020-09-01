package com.github.jensborch.webhooks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link WebhookConfiguration}.
 */
class WebhookConfigurationTest {

    @Test
    void testSyncFrom() throws Exception {
        WebhookConfiguration configuration = new WebhookConfiguration() {
            @Override
            public long getTimeToLive() {
                return 1;
            }

            @Override
            public TimeUnit getTimeToLiveUnit() {
                return TimeUnit.MINUTES;
            }

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
