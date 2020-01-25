package dk.jensborch.webhooks;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import dk.jensborch.webhooks.consumer.Consumer;
import dk.jensborch.webhooks.publisher.Publisher;

/**
 *
 */
@Dependent
public class ClientProducer {

    @Produces
    @Consumer
    @Publisher
    public Client getClient() {
        
        return ClientBuilder.newClient()
                .register(new ObjectMapperProvider())
                .register(LoggingFilter.class);
    }

}
