package dk.jensborch.webhooks;

import java.net.URI;
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
}
