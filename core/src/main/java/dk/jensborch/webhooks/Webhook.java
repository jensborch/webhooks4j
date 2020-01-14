package dk.jensborch.webhooks;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Value;

/**
 *
 */
@Value
public class Webhook {

    UUID id;
    URI publisher;
    Set<String> topics;

    public Webhook(final URI publisher, final Set<String> topics) {
        this.id = UUID.randomUUID();
        this.publisher = publisher;
        this.topics = new HashSet<>(topics);
    }
}
