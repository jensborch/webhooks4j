package dk.jensborch.webhooks.consumer;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapStatusRepository;

/**
 *
 */
@Consumer
@ApplicationScoped
public class ConsumerStatusRepository extends HashMapStatusRepository {
}
