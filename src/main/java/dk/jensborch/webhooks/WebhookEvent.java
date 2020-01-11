package dk.jensborch.webhooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.Value;

/**
 *
 */
@Value
public class WebhookEvent {

    UUID id;
    String topic;
    Map<String, Object> data;

    public WebhookEvent(String topic, Map<String, Object> data) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.data = new HashMap<>(data);
    }
}
