package com.github.jensborch.webhooks;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Webhooks4j events time to live (TTL). Users of Webhooks4j
 * must implement a CDI producer that returns a implementation of this
 * interface.
 * <p>
 * Note: This interface is only used by the MongoDB modules, but could be used
 * by additional persistence implementations. Thus depending on the persistence
 * implementation used, it is optional to implement a producer for the
 * interface.
 * </p>
 *
 */
public interface WebhookTTLConfiguration {

    /**
     * Time to live (TTL) for webhook events in the events repository.
     *
     * @return the TTL
     */
    long getAmount();

    /**
     * Time unit used for TTL.
     *
     * @return the time unit
     */
    TimeUnit getUnit();

}
