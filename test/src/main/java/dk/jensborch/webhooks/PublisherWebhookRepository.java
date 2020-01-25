package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@ApplicationScoped
@Publisher
public class PublisherWebhookRepository extends HashMapWebhookRepository {

}
