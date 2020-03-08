package dk.jensborch.webhooks;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import dk.jensborch.webhooks.subscriber.SubscriberEventExposure;
import dk.jensborch.webhooks.subscriber.SubscriberWebhooksExposure;
import dk.jensborch.webhooks.publisher.PublisherEventExposure;
import dk.jensborch.webhooks.publisher.PublisherWebhookExposure;

/**
 * Defines the test application.
 */
public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Arrays.stream(new Class<?>[]{
            SubscriberEventExposure.class,
            SubscriberWebhooksExposure.class,
            PublisherEventExposure.class,
            PublisherWebhookExposure.class})
                .collect(Collectors.toSet());
    }

}
