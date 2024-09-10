package com.github.jensborch.webhooks;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.github.jensborch.webhooks.subscriber.WebhookSyncConfiguration;

/**
 * Webhooks test configuration.
 */
@ApplicationScoped
public class WebhookConfigurationProducer {

    @Produces
    public WebhookSyncConfiguration getSyncConfiguration() {
        return new WebhookSyncConfiguration() {

            @Override
            public long getSyncOffset() {
                return 1;
            }

            @Override
            public TemporalUnit getSyncOffsetUnit() {
                return ChronoUnit.DAYS;
            }
        };
    }

    @Produces
    public WebhookTTLConfiguration getTTLConfiguration() {
        return new WebhookTTLConfiguration() {
            @Override
            public long getAmount() {
                return 100L;
            }

            @Override
            public TimeUnit getUnit() {
                return TimeUnit.DAYS;
            }
        };
    }

}
