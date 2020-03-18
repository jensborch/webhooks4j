package com.github.jensborch.webhooks;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.Application;

import com.github.jensborch.webhooks.subscriber.SubscriberEventExposure;
import com.github.jensborch.webhooks.subscriber.SubscriberWebhooksExposure;
import com.github.jensborch.webhooks.publisher.PublisherEventExposure;
import com.github.jensborch.webhooks.publisher.PublisherWebhookExposure;

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
