package dk.jensborch.webhooks.consumer;

import dk.jensborch.webhooks.HashMapWebhookRepository;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.consumer.Consumer;

/**
 *
 */
@ApplicationScoped
@Consumer
public class ConsumerWebhookRepository extends HashMapWebhookRepository {

}
