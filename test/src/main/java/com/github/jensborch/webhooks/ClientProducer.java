package com.github.jensborch.webhooks;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import com.github.jensborch.webhooks.publisher.Publisher;
import com.github.jensborch.webhooks.subscriber.Subscriber;

/**
 * CDI producer for getting a JAX-RS client.
 */
@Dependent
public class ClientProducer {

    @Produces
    @Publisher
    public Client getPublisherClient() {
        return ClientBuilder
                .newClient()
                .register(ObjectMapperProvider.class)
                .register(new BasicAuthClientRequestFilter("publisher", "pubpub"));
    }

    @Produces
    @Subscriber
    public Client getSubscriberClient() {
        return ClientBuilder
                .newClient()
                .register(ObjectMapperProvider.class)
                .register(new BasicAuthClientRequestFilter("subscriber", "concon"));
    }

}
