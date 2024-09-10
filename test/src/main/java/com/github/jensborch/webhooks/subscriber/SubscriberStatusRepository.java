package com.github.jensborch.webhooks.subscriber;


import jakarta.enterprise.context.ApplicationScoped;

import com.github.jensborch.webhooks.HashMapStatusRepository;

/**
 * Test repository implementation.
 */
@Subscriber
@ApplicationScoped
public class SubscriberStatusRepository extends HashMapStatusRepository {
}
