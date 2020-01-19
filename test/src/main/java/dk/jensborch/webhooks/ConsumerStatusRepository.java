package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.consumer.Consumer;

/**
 *
 */
@Consumer
@ApplicationScoped
public class ConsumerStatusRepository extends HashMapStatusRepository {
}
