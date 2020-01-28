package dk.jensborch.webhooks.publisher;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.HashMapStatusRepository;

/**
 *
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends HashMapStatusRepository {
}
