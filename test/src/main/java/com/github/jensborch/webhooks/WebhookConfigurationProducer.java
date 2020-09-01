package com.github.jensborch.webhooks;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * Webhooks test configuration.
 */
@ApplicationScoped
public class WebhookConfigurationProducer {

    @Produces
    public WebhookConfiguration getWebhookConfiguration() {
        return new WebhookConfiguration() {
            @Override
            public long getTimeToLive() {
                return 100L;
            }

            @Override
            public TimeUnit getTimeToLiveUnit() {
                return TimeUnit.DAYS;
            }

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

}
