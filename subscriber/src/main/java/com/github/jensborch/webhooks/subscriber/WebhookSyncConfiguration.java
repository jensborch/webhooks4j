package com.github.jensborch.webhooks.subscriber;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;

import com.github.jensborch.webhooks.Webhook;

/**
 * Configuration for Webhooks4j event synchronisation. Users of Webhooks4j must
 * implement a CDI producer that returns a implementation of this interface.
 */
public interface WebhookSyncConfiguration {

    /**
     * The synchronisation offset to use when synchronizing old events. This
     * value will be deducted from {@link Webhook#getUpdated()} when
     * synchronizing. The value should correspond to the maximum expected
     * downtime for publisher and subscriber.
     *
     * @return the synchronisation offset
     */
    long getSyncOffset();

    /**
     * The temporal unit to use for the synchronisation offset.
     *
     * @return synchronisation offset temporal unit
     */
    TemporalUnit getSyncOffsetUnit();

    default ZonedDateTime syncFrom(Webhook webhook) {
        return webhook.getUpdated().minus(getSyncOffset(), getSyncOffsetUnit());
    }

}
