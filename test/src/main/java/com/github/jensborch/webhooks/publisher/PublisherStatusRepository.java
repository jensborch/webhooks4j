package com.github.jensborch.webhooks.publisher;


import jakarta.enterprise.context.ApplicationScoped;

import com.github.jensborch.webhooks.HashMapStatusRepository;

/**
 * Test repository implementation.
 */
@Publisher
@ApplicationScoped
public class PublisherStatusRepository extends HashMapStatusRepository {
}
