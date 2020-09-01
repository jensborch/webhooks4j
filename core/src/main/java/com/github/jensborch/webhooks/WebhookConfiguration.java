package com.github.jensborch.webhooks;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for Webhooks4j. Users of Webhooks4j must implement a CDI
 * producer to create a implementation of this class.
 * <p>
 * Note: The configuration interface is currently only needed by the MongoDB
 * modules.
 * </p>
 *
 */
public interface WebhookConfiguration {

    long getTimeToLive();

    TimeUnit getTimeToLiveTimeUnit();

}
