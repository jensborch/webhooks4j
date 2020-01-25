package dk.jensborch.webhooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;


/**
 *
 */
//@Value
@AllArgsConstructor
public class WebhookEvent {

    @NotNull
    public UUID id;
    @NotNull
    public String topic;
    @NotNull
    public Map<String, Object> data;

    public WebhookEvent(final String topic, final Map<String, Object> data) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.data = new HashMap<>(data);
    }

    public String getTopic() {
        return topic;
    }

    public UUID getId() {
        return id;
    }

}
