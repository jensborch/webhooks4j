package dk.jensborch.webhooks;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import dk.jensborch.webhooks.consumer.Consumer;
import dk.jensborch.webhooks.publisher.Publisher;

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
                .register(new BasicAuthClientRequestFilter("publisher", "pubpub"));
    }

    @Produces
    @Consumer
    public Client getConsumerClient() {
        return ClientBuilder
                .newClient()
                .register(new BasicAuthClientRequestFilter("publisher", "pubpub"));
    }

}
