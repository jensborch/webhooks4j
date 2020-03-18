package com.github.jensborch.webhooks.publisher;

import javax.enterprise.context.ApplicationScoped;

import com.github.jensborch.webhooks.HashMapStatusRepository;

/**
 *
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends HashMapStatusRepository {
}
