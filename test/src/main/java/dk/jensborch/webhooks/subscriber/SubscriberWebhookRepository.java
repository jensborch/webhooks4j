package dk.jensborch.webhooks.subscriber;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapWebhookRepository;

/**
 *
 */
@ApplicationScoped
@Subscriber
public class SubscriberWebhookRepository extends HashMapWebhookRepository {

}
