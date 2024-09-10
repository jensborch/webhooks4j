package com.github.jensborch.webhooks.publisher;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.github.jensborch.webhooks.HashMapWebhookRepository;
import com.github.jensborch.webhooks.WebhookEventStatus;

/**
 * Test repository implementation.
 */
@ApplicationScoped
@Publisher
public class PublisherWebhookRepository extends HashMapWebhookRepository {

    @Inject
    @Publisher
    PublisherStatusRepository repo;

    @Override
    public void touch(final UUID id) {
        touch(id, repo.lastFailed().map(WebhookEventStatus::getStart).orElse(null));
    }

}
