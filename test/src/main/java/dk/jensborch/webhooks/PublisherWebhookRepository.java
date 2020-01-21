package dk.jensborch.webhooks;

import javax.enterprise.context.Dependent;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@Dependent
@Publisher
public class PublisherWebhookRepository extends HashMapWebhookRepository {

}
