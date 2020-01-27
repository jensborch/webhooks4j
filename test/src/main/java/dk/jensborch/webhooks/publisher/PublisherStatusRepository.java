package dk.jensborch.webhooks.publisher;

import dk.jensborch.webhooks.HashMapStatusRepository;

import javax.enterprise.context.ApplicationScoped;

import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends HashMapStatusRepository {
}
