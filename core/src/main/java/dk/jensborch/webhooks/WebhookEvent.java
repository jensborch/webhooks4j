package dk.jensborch.webhooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * A event emitted by a publisher on a given topic.
 */
@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent {

    @NotNull
    UUID id;
    @NotNull
    String topic;
    @NotNull
    Map<String, Object> data;

    public WebhookEvent(final String topic, final Map<String, Object> data) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.data = new HashMap<>(data);
    }

}
