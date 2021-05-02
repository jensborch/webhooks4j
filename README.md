# Webhooks4j

Small, simple and extendable Java library for messaging using webhooks and CDI events.

## Status

[![Java CI with Maven](https://github.com/jensborch/webhooks4j/actions/workflows/maven.yml/badge.svg)](https://github.com/jensborch/webhooks4j/actions/workflows/maven.yml)

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.jensborch.webhooks4j%3Awebhooks4j&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.jensborch.webhooks4j%3Awebhooks4j)

Webhooks4j is currently under development.

## Introduction

Webhooks4j is a simple Java library for implementing messaging using webhooks and event-sourcing, that does not need any infrastructure. It is meant to work for simple use cases where message brokers like [Kafka](https://kafka.apache.org/) are not needed. The library is based on the publish–subscribe pattern.

To subscribe to a topic, inject `WebhookSubscriptions` and call the subscribe method:

```Java
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.subscriber.WebhookSubscriptions;

@Inject
WebhookSubscriptions subscriptions;

Webhook webhook = new Webhook(new URI("http://publisher-host/context-root"), new URI("http://subscriber-host/context-root"), "my-topic");
subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
```

To publish events, inject a `WebhookPublisher` and call the publish method:

```Java
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.publisher.WebhookPublisher;

@Inject
WebhookPublisher publisher;

Map<String, Object> eventData = new HashMap<>();
publisher.publish("my-topic", eventData));
```

To receive event use the CDI `@Observes` annotation:

```Java
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventTopic;

public void observe(@Observes @WebhookEventTopic("my-topic") final WebhookEvent event) {
    //Process the event
}
```

The library is build using [CDI 1.2](http://www.cdi-spec.org/), [JAX-RS 2.0](https://github.com/jax-rs), [Jackson](https://github.com/FasterXML/jackson) and [SLF4J](http://www.slf4j.org/). Usage of SLF4J version 1.6.0 or higher is assumed.

CDI 1.2 is used to be compatible with as many application servers as possible. This imposes some constraints and the solution thus currently do not support asynchronous CDI events and generic event data.

## Getting started

Added the following dependency for a subscriber:

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-subscriber</artifactId>
    <version>0.6.5</version>
</dependency>
```

and

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-subscriber</artifactId>
    <version>0.6.5</version>
</dependency>
```

for a publisher.

For MongoDB support add:

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-mongodb-subscriber</artifactId>
    <version>0.6.5</version>
</dependency>
```

and/or

```xml
<dependency>
    <groupId>com.github.jensborch.webhooks4j</groupId>
    <artifactId>webhooks4j-mongodb-publisher</artifactId>
    <version>0.6.5</version>
</dependency>
```

If MongoDB is not use for persistence, it is necessary to implement `WebhookEventStatusRepository` and `WebhookRepository` repository interfaces.

The MongoDB dependency requires [POJO](https://mongodb.github.io/mongo-java-driver/3.12/bson/pojos/) support, see examples below.

CDI producers must be defined for:

- javax.ws.rs.client.Client
- com.github.jensborch.webhooks.WebhookTTLConfiguration (required for MongoDB, otherwise optional)
- com.github.jensborch.webhooks.subscriber.WebhookSyncConfiguration (required only by subscriber module)
- com.mongodb.client.MongoDatabase (for MongoDB support)

and the following REST exposure classes:

- com.github.jensborch.webhooks.subscriber.SubscriberEventExposure
- com.github.jensborch.webhooks.subscriber.SubscriberWebhooksExposure
- com.github.jensborch.webhooks.publisher.PublisherEventExposure
- com.github.jensborch.webhooks.publisher.PublisherWebhookExposure

should be registered in your JAX-RS application class, depending on how you do JAX-RS configuration.

Additionally it might be necessary to configure Jackson by implementing ContextResolver<ObjectMapper>, as the `Jdk8Module` and `JavaTimeModule` are required.

An example application using [Quarkus](https://quarkus.io/) can be found in the Maven test module.

### Examples

JAX-RS client producer:

```Java
@Dependent
public class ClientProducer {

    @Produces
    @Publisher
    public Client getPublisherClient() {
        return ClientBuilder.newClient();
    }

    @Produces
    @Subscriber
    public Client getSubscriberClient() {
        return ClientBuilder.newClient();
    }

}
```

Mongo database producer:

```Java
@ApplicationScoped
public class WebhookMongoDBProducer {

    @Inject
    private MongoClient client;

    @Produces
    @Subscriber
    @Publisher
    public MongoDatabase mongoDatabase() {
         return client.getDatabase("MyDatabase");
    }

}
```

Note, this requires an additional CDI producer for `MongoClient`, but it is possible to configure a `MongoClient` directly instead. If no codecs for `ZonedDateTime` and `URI` exists, register the following codec class:

- com.github.jensborch.webhooks.mongodb.URICodec
- com.github.jensborch.webhooks.mongodb.ZonedDateTimeCode

JAX-RS application class:

```Java
@ApplicationPath("/")
public class MyApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>(Arrays.asList(
                MyExposure.class,
                SubscriberEventExposure.class,
                SubscriberWebhooksExposure.class,
                PublisherEventExposure.class,
                PublisherWebhookExposure.class,
        ));
        return classes;
    }
}
```

Jackson ContextResolver class:

```Java
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

    @Override
    public ObjectMapper getContext(final Class<?> objectType) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}
```

## Security

All endpoints are secured using JAX-RS roles. To access subscriber end-point, the __subscriber__ role is needed. To access publisher endpoints the __publisher__ role is need. The are some exceptions to this, as a __publisher__ is allowed to POST callback events to a subscriber end-point. Refer to the Swagger documentation for the [publisher](../master/publisher-swagger.yaml) and [subscriber](../master/subscriber-swagger.yaml) for details.

When creating the JAX-RS Client CDI producer, filters should be added to handle security correctly. A simple HTTP Basic access authentication filter can be found in the Maven test module.

## Building

The Webhooks4j is build using Maven.

To build the application run the following command:

```sh
./mvnw package
```

Install the application in your local maven repository (required for running locally)

```sh
./mvnw install
```

Start the test application using:

```sh
./mvnw compile -pl test quarkus:dev
```

Call endpoints using VSCode RestClient or similar. See webhooks4j.http

Run mutation tests:

```sh
./mvnw eu.stamp-project:pitmp-maven-plugin:run
```

Release to Maven central:

```sh
./mvnw release:clean release:prepare -Prelease
./mvnw release:perform -Prelease
````
