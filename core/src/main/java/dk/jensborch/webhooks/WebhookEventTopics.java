package dk.jensborch.webhooks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a list of event topics. Used as a query parameter in exposure
 * classes.
 */
public final class WebhookEventTopics {

    private final List<String> topics;

    private WebhookEventTopics(final String topics) {
        this.topics = topics == null
                ? new ArrayList<>(0)
                : Arrays
                        .stream(topics.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
    }

    public static WebhookEventTopics parse(final String topics) {
        return new WebhookEventTopics(topics);
    }

    public String[] getTopics() {
        return topics.toArray(new String[]{});
    }

}
