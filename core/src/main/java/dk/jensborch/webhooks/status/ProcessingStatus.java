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
public class ProcessingStatus {

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

    public ProcessingStatus done(final boolean sucess) {
        if (sucess) {
            status = Status.SUCCESS;
        } else {
            status = Status.FAILD;
        }
        end = ZonedDateTime.now();
        return this;
    }

    public boolean eligible() {
        return status == Status.FAILD || status == Status.STARTED;
    }

    /**
     * The processing status of the event.
     */
    public enum Status {
        STARTED, FAILD, SUCCESS;
    }
}
