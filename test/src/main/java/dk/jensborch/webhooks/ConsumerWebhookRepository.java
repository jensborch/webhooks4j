package dk.jensborch.webhooks;

import javax.enterprise.context.Dependent;

import dk.jensborch.webhooks.consumer.Consumer;

/**
 *
 */
@Dependent
@Consumer
public class ConsumerWebhookRepository extends HashMapWebhookRepository {

}
