package dk.jensborch.webhooks;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Value;

/**
 *
 */
@Value
public class Webhook {

    UUID id;
    URI publisher;
    Set<String> topics;

    protected Webhook() {
        this.id = null;
        this.publisher = null;
        this.topics = new HashSet<>();
    }

    public Webhook(final URI publisher, final Set<String> topics) {
        this.id = UUID.randomUUID();
        this.publisher = publisher;
        this.topics = new HashSet<>(topics);
    }

    public Webhook(final URI publisher, final String... topics) {
        this(publisher, Arrays.stream(topics).collect(Collectors.toSet()));
    }
}
