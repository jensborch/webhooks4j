package com.github.jensborch.webhooks.subscriber;

import javax.enterprise.context.ApplicationScoped;

import com.github.jensborch.webhooks.HashMapWebhookRepository;

/**
 *
 */
@ApplicationScoped
@Subscriber
public class SubscriberWebhookRepository extends HashMapWebhookRepository {

}
