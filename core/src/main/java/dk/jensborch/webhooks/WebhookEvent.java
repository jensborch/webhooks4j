package dk.jensborch.webhooks;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.validation.constraints.NotNull;

/**
 * A event emitted by a publisher on a given topic.
 */
public class WebhookEvent {

    @NotNull
    private final UUID id;

    @NotNull
    private final UUID publisher;

    @NotNull
    private final String topic;

    @NotNull
    private final Map<String, Object> data;

    @ConstructorProperties({"id", "publisher", "topic", "data"})
    protected WebhookEvent(final UUID id, final UUID publisher, final String topic, final Map<String, Object> data) {
        this.id = id;
        this.publisher = publisher;
        this.topic = topic;
        this.data = data == null ? new HashMap<>() : new HashMap<>(data);
    }

    public WebhookEvent(final UUID publisher, final String topic, final Map<String, Object> data) {
        this(UUID.randomUUID(), publisher, topic, data);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPublisher() {
        return publisher;
    }

    public String getTopic() {
        return topic;
    }

    public Map<String, Object> getData() {
        return Collections.unmodifiableMap(data);
    }

    @Override
    public String toString() {
        return "WebhookEvent{" + "id=" + id + ", publisher=" + publisher + ", topic=" + topic + ", data=" + data + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.id);
        hash = 23 * hash + Objects.hashCode(this.publisher);
        hash = 23 * hash + Objects.hashCode(this.topic);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final WebhookEvent other = (WebhookEvent) obj;
        return Objects.equals(this.topic, other.topic)
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.publisher, other.publisher);
    }

}
