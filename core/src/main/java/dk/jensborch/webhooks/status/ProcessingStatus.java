package dk.jensborch.webhooks.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import dk.jensborch.webhooks.WebhookEvent;
import lombok.Data;
import lombok.Setter;

/**
 * Processing status for a webhook event.
 */
@Data
public class ProcessingStatus implements Comparable<ProcessingStatus> {

    @Setter
    ZonedDateTime end;
    @Setter
    Status status;

    private final UUID id;
    private final WebhookEvent event;
    private final ZonedDateTime start;
    private final UUID webhook;

    public ProcessingStatus(final WebhookEvent event, final UUID webhook) {
        this.id = event.getId();
        this.event = event;
        this.start = ZonedDateTime.now();
        this.status = Status.STARTED;
        this.webhook = webhook;
    }

    public ProcessingStatus done(final boolean success) {
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
    public int compareTo(final ProcessingStatus other) {
        return other.start.compareTo(this.start);
    }

    /**
     * The processing status of the event.
     */
    public enum Status {
        STARTED, FAILED, SUCCESS
    }
}
