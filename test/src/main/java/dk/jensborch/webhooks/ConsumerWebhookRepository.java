package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.consumer.Consumer;

/**
 *
 */
@ApplicationScoped
@Consumer
public class ConsumerWebhookRepository extends HashMapWebhookRepository {

}
