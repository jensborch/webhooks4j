package dk.jensborch.webhooks;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import dk.jensborch.webhooks.consumer.ConsumerEventExposure;
import dk.jensborch.webhooks.consumer.ConsumerWebhooksExposure;
import dk.jensborch.webhooks.publisher.PublisherWebhookExposure;

/**
 *
 */
public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Arrays.stream(new Class<?>[]{
            DefaultExceptionMapper.class,
            WebhookExceptionMapper.class,
            ConstraintViolationExceptionMapper.class,
            ConsumerEventExposure.class,
            ConsumerWebhooksExposure.class,
            PublisherWebhookExposure.class})
                .collect(Collectors.toSet());
    }

}
