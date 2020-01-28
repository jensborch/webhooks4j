package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapWebhookRepository;

/**
 *
 */
@ApplicationScoped
@Consumer
public class ConsumerWebhookRepository extends HashMapWebhookRepository {

}
