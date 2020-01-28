package dk.jensborch.webhooks.publisher;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapWebhookRepository;

/**
 *
 */
@ApplicationScoped
@Publisher
public class PublisherWebhookRepository extends HashMapWebhookRepository {

}
