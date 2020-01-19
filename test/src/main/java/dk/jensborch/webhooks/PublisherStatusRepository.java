package dk.jensborch.webhooks;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends HashMapStatusRepository {
}
