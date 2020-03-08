package dk.jensborch.webhooks.subscriber;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapStatusRepository;

/**
 *
 */
@Subscriber
@ApplicationScoped
public class SubscriberStatusRepository extends HashMapStatusRepository {
}
