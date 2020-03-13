package dk.jensborch.webhooks;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

/**
 * This class defines a Webhook with a publisher and subscribe URI.
 */
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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

    public Webhook(final URI publisher, final URI subscriber, final Set<String> topics) {
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

}
