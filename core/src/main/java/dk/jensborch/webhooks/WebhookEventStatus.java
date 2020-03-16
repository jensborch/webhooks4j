package dk.jensborch.webhooks;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Processing status for a webhook event.
 */
@Data
@NoArgsConstructor(force = true)
public class WebhookEventStatus implements Comparable<WebhookEventStatus> {

    @Setter
    ZonedDateTime end;
    @Setter
    Status status;

    private final UUID id;
    private final WebhookEvent event;
    private final ZonedDateTime start;
    private final UUID webhook;

    public WebhookEventStatus(final WebhookEvent event, final UUID webhook) {
        this.id = event.getId();
        this.event = event;
        this.start = ZonedDateTime.now();
        this.status = Status.STARTED;
        this.webhook = webhook;
    }

    public WebhookEventStatus done(final boolean success) {
        if (success) {
            status = Status.SUCCESS;
        } else {
            status = Status.FAILED;
        }
        end = ZonedDateTime.now();
        return this;
    }

    public boolean eligible() {
        return status == Status.FAILED || status == Status.STARTED;
    }

    @Override
    public int compareTo(final WebhookEventStatus other) {
        return other.start.compareTo(this.start);
    }

    /**
     * The processing status of the event.
     */
    public enum Status {
        STARTED, FAILED, SUCCESS
    }
}
