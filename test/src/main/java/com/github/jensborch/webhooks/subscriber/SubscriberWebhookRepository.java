package com.github.jensborch.webhooks.subscriber;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.github.jensborch.webhooks.HashMapWebhookRepository;
import com.github.jensborch.webhooks.WebhookEventStatus;

/**
 * Test repository implementation.
 */
@ApplicationScoped
@Subscriber
public class SubscriberWebhookRepository extends HashMapWebhookRepository {

    @Inject
    @Subscriber
    SubscriberStatusRepository repo;

    @Override
    public void touch(final UUID id) {
        touch(id, repo.lastFailed().map(WebhookEventStatus::getStart).orElse(null));
    }
}
