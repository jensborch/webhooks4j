package com.github.jensborch.webhooks;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Application;

import com.github.jensborch.webhooks.subscriber.SubscriberEventExposure;
import com.github.jensborch.webhooks.subscriber.SubscriberWebhookExposure;
import com.github.jensborch.webhooks.exceptionmappers.ConstraintViolationExceptionMapper;
import com.github.jensborch.webhooks.exceptionmappers.WebhookExceptionMapper;
import com.github.jensborch.webhooks.publisher.PublisherEventExposure;
import com.github.jensborch.webhooks.publisher.PublisherWebhookExposure;

/**
 * Defines the test application.
 */
public class TestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return Arrays.stream(new Class<?>[]{
            WebhookExceptionMapper.class,
            ConstraintViolationExceptionMapper.class,
            DefaultExceptionMapper.class,
            SubscriberEventExposure.class,
            SubscriberWebhookExposure.class,
            PublisherEventExposure.class,
            PublisherWebhookExposure.class})
                .collect(Collectors.toSet());
    }

}
