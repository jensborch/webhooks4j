package dk.jensborch.webhooks.consumer;

import dk.jensborch.webhooks.HashMapStatusRepository;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.consumer.Consumer;

/**
 *
 */
@Consumer
@ApplicationScoped
public class ConsumerStatusRepository extends HashMapStatusRepository {
}
