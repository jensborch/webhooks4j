# Webhooks4j

Small, simple and extendable Java library for messaging using webhooks and CDI events.

## Status

[![Build Status](https://travis-ci.org/jensborch/webhooks4j.svg?branch=master)](https://travis-ci.org/jensborch/webhooks4j) [![codecov](https://codecov.io/gh/jensborch/webhooks4j/branch/master/graph/badge.svg)](https://codecov.io/gh/jensborch/webhooks4j)

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.jensborch.webhooks4j%3Awebhooks4j&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.jensborch.webhooks4j%3Awebhooks4j)

## Introduction

Webhooks4j is a simple Java library for implementing messaging using webhooks and event-sourcing, that does not need any infrastructure. It is meant to work for simple use cases where message brokers like [Kafka](https://kafka.apache.org/) are not needed. The library is based on the publish–subscribe pattern.

To subscribe to to a topic, inject ```WebhookSubscriptions``` and call the subscribe method:

```Java
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.subscriber.WebhookSubscriptions;

@Inject
WebhookSubscriptions subscriptions;

Webhook webhook = new Webhook(new URI("http://publisher-host/context-root"), new URI("http://subscriber-host/context-root"), "my-topic");
subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
```

To publish an events events inject a ```WebhookPublisher``` and call the publish method:

```Java
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.publisher.WebhookPublisher;

@Inject
WebhookPublisher publisher;

Map<String, Object> eventData = new HashMap<>();
publisher.publish(new WebhookEvent(webhook.getId(), "my-topic", eventData));
```

To receive event use the CDI ```@Observes``` annotation:

```Java
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventTopic;

public void observe(@Observes @WebhookEventTopic("my-topic") final WebhookEvent event) {
    //Process the event
}
```

The library build using [CDI 1.2](http://www.cdi-spec.org/), [JAX-RS 2.0](https://github.com/jax-rs) and [Jackson](https://github.com/FasterXML/jackson). CDI 1.2 is used to be compatible with as many application servers as possible. This imposes some constraints on the solution and the solution thus currently do not support asynchronous CDI events and generic event data.

## Getting started

Added the following dependency:

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-core</artifactId>
    <version>0.5.8</version>
</dependency>
```

For MongoDB support:

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-mongodb</artifactId>
    <version>0.5.8</version>
</dependency>
```

## Building

The Webhooks4j is build using Maven.

To build the application run the following command:

```sh
./mvnw package
```

Start the test application using:

```sh
./mvnw compile -pl test quarkus:dev
```

Run mutation tests:

```sh
./mvnw eu.stamp-project:pitmp-maven-plugin:run
```

Release to Maven central:

```sh
./mvnw release:clean release:prepare -Prelease
./mvnw release:perform -Prelease
````