package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@Publisher
@ApplicationScoped
public class PublisherWebhookRepository extends HashMapWebhookRepository {
}
