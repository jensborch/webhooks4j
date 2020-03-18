package com.github.jensborch.webhooks;

import java.beans.ConstructorProperties;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class defines a Webhook with a publisher and subscribe URI.
 */
public class Webhook {

    @NotNull
    private final UUID id;

    @NotNull
    private final URI publisher;

    @NotNull
    private final URI subscriber;

    @NotNull
    private final ZonedDateTime created;

    @NotNull
    private State state;

    @NotNull
    @Size(min = 1)
    private Set<String> topics;

    @NotNull
    private ZonedDateTime updated;

    @ConstructorProperties({"id", "publisher", "subscriber", "state", "topics", "created", "updated"})
    protected Webhook(final UUID id, final URI publisher, final URI subscriber, final State state,
            final Set<String> topics, final ZonedDateTime created, final ZonedDateTime updated) {
        this.state = state;
        this.topics = topics == null ? new HashSet<>() : new HashSet<>(topics);
        this.id = id == null ? UUID.randomUUID() : id;
        this.publisher = publisher;
        this.subscriber = subscriber;
        this.created = created == null ? ZonedDateTime.now() : created;
        this.updated = updated == null ? this.created : updated;
    }

    public Webhook(final URI publisher, final URI subscriber, final Set<String> topics) {
        this(null, publisher, subscriber, State.ACTIVE, topics, null, null);
    }

    public Webhook(final URI publisher, final URI subscriber, final String... topics) {
        this(publisher, subscriber, Arrays.stream(topics).collect(Collectors.toSet()));
    }

    public State getState() {
        return state;
    }

    public ZonedDateTime getUpdated() {
        return updated;
    }

    public Set<String> getTopics() {
        return Collections.unmodifiableSet(topics);
    }

    public UUID getId() {
        return id;
    }

    public URI getPublisher() {
        return publisher;
    }

    public URI getSubscriber() {
        return subscriber;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    @JsonIgnore
    public Endpoints getSubscriberEndpoints() {
        return new SubscriberEndpoints(subscriber);
    }

    @JsonIgnore
    public Endpoints gePublisherEndpoints() {
        return new PublisherEndpoints(publisher);
    }

    public Webhook state(final State state) {
        this.state = state;
        return this;
    }

    public Webhook updated(final ZonedDateTime updated) {
        this.updated = updated;
        return this;
    }

    public Webhook touch() {
        this.updated = ZonedDateTime.now();
        return this;
    }

    public Webhook topics(final Collection<String> topics) {
        this.topics = new HashSet<>(topics);
        return this;
    }

    @JsonIgnore
    public boolean isActive() {
        return state == State.ACTIVE || state == State.FAILED;
    }

    @Override
    public String toString() {
        return "Webhook{" + "state=" + state
                + ", updated=" + updated
                + ", topics=" + topics
                + ", id=" + id
                + ", publisher=" + publisher
                + ", subscriber=" + subscriber
                + ", created=" + created + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.topics);
        hash = 41 * hash + Objects.hashCode(this.publisher);
        hash = 41 * hash + Objects.hashCode(this.subscriber);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Webhook other = (Webhook) obj;
        return Objects.equals(this.topics, other.topics)
                && Objects.equals(this.publisher, other.publisher)
                && Objects.equals(this.subscriber, other.subscriber);
    }

    /**
     * Webhook status.
     */
    public enum State {
        ACTIVE, INACTIVE, SUBSCRIBE, SUBSCRIBING, SYNCHRONIZE, SYNCHRONIZING, UNSUBSCRIBE, UNSUBSCRIBING, FAILED
    }

    /**
     * Subscriber/publisher endpoints
     */
    public abstract static class Endpoints {

        private final URI webhooks;
        private final URI events;

        public Endpoints(final URI webhooks, final URI events) {
            this.webhooks = webhooks;
            this.events = events;
        }

        public URI getEvents() {
            return events;
        }

        public URI getWebhooks() {
            return webhooks;
        }

    }

    /**
     * Subscriber endpoints.
     */
    public static class SubscriberEndpoints extends Endpoints {

        public static final String WEBHOOKS_PATH = "subscriber-webhooks";
        public static final String EVENTS_PATH = "subscriber-events";

        public SubscriberEndpoints(final URI contextRoot) {
            super(
                    UriBuilder.fromUri(contextRoot).path(WEBHOOKS_PATH).build(),
                    UriBuilder.fromUri(contextRoot).path(EVENTS_PATH).build()
            );
        }

    }

    /**
     * Publisher endpoints.
     */
    public static class PublisherEndpoints extends Endpoints {

        public static final String WEBHOOKS_PATH = "publisher-webhooks";
        public static final String EVENTS_PATH = "publisher-events";

        public PublisherEndpoints(final URI contextRoot) {
            super(
                    UriBuilder.fromUri(contextRoot).path(WEBHOOKS_PATH).build(),
                    UriBuilder.fromUri(contextRoot).path(EVENTS_PATH).build()
            );
        }
    }

}
