package dk.jensborch.webhooks;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

/**
 * This class defines a Webhook with a publisher and subscribe URI.
 */
@Data
//@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Webhook {

    @NotNull
    @Setter(AccessLevel.NONE)
    private State state;

    @NotNull
    @Setter(AccessLevel.NONE)
    private ZonedDateTime updated;

    @NotNull
    @Size(min = 1)
    @Setter(AccessLevel.NONE)
    private Set<String> topics;

    @NotNull
    private final UUID id;

    @NotNull
    private final URI publisher;

    @NotNull
    private final URI subscriber;

    @NotNull
    private final ZonedDateTime created;

    @JsonCreator
    public Webhook(@JsonProperty("publisher") final URI publisher, @JsonProperty("subscriber") final URI subscriber, @JsonProperty("topics") final Set<String> topics) {
        this.state = State.ACTIVE;
        this.id = UUID.randomUUID();
        this.subscriber = subscriber;
        this.publisher = publisher;
        this.topics = new HashSet<>(topics);
        this.created = ZonedDateTime.now();
        this.updated = this.created;
    }

    public Webhook(final URI publisher, final URI subscriber, final String... topics) {
        this(publisher, subscriber, Arrays.stream(topics).collect(Collectors.toSet()));
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

    /**
     * Webhook status.
     */
    public enum State {
        ACTIVE, INACTIVE, SUBSCRIBE, SUBSCRIBING, SYNCHRONIZE, SYNCHRONIZING, UNSUBSCRIBE, UNSUBSCRIBING, FAILED
    }

    /**
     *
     */
    public interface Endpoints {

        URI getEvents();

        URI getWebhooks();

    }

    /**
     *
     */
    @Data
    public static class SubscriberEndpoints implements Endpoints {

        public static final String WEBHOOKS_PATH = "subscriber-webhooks";
        public static final String EVENTS_PATH = "subscriber-events";

        private final URI webhooks;
        private final URI events;

        public SubscriberEndpoints(final URI contextRoot) {
            Objects.requireNonNull(contextRoot, "Context root must be defined");
            webhooks = UriBuilder.fromUri(contextRoot).path(WEBHOOKS_PATH).build();
            events = UriBuilder.fromUri(contextRoot).path(EVENTS_PATH).build();
        }

    }

    /**
     *
     */
    @Data
    public static class PublisherEndpoints implements Endpoints {

        public static final String WEBHOOKS_PATH = "publisher-webhooks";
        public static final String EVENTS_PATH = "publisher-events";

        private final URI webhooks;
        private final URI events;

        public PublisherEndpoints(final URI contextRoot) {
            Objects.requireNonNull(contextRoot, "Context root must be defined");
            webhooks = UriBuilder.fromUri(contextRoot).path(WEBHOOKS_PATH).build();
            events = UriBuilder.fromUri(contextRoot).path(EVENTS_PATH).build();
        }
    }

}
