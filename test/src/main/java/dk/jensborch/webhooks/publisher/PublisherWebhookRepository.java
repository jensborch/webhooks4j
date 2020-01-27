package dk.jensborch.webhooks.publisher;

import dk.jensborch.webhooks.HashMapWebhookRepository;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@ApplicationScoped
@Publisher
public class PublisherWebhookRepository extends HashMapWebhookRepository {

}
