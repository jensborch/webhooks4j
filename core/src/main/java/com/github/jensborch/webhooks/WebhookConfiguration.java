package com.github.jensborch.webhooks;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for Webhooks4j. Users of Webhooks4j must implement a CDI
 * producer that returns implementation of this interface.
 * <p>
 * Note: The TTL values are only used by the MongoDB modules, but could be used
 * by additional persistence implementations.
 * </p>
 *
 */
public interface WebhookConfiguration {

    /**
     * Time to live (TTL) for webhook events in the events repository.
     *
     * @return the TTL
     */
    long getTimeToLive();

    /**
     * Time unit used for TTL.
     *
     * @return the time unit
     */
    TimeUnit getTimeToLiveTimeUnit();

    /**
     * The synchronisation offset to use when synchronizing old events.
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
