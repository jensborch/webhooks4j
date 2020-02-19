package dk.jensborch.webhooks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a list of event topics. Used as a query parameter in exposure
 * classes.
 */
public final class WebhookEventTopics {

    private final Set<String> topics;

    private WebhookEventTopics(final String topics) {
        this.topics = topics == null
                ? new HashSet<>(0)
                : Arrays
                        .stream(topics.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());
    }

    public static WebhookEventTopics parse(final String topics) {
        return new WebhookEventTopics(topics);
    }

    public String[] getTopics() {
        return topics.toArray(new String[]{});
    }

}
