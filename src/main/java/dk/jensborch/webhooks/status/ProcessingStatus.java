package dk.jensborch.webhooks.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import dk.jensborch.webhooks.WebhookEvent;
import lombok.Data;
import lombok.Setter;

/**
 *
 */
@Data
public class ProcessingStatus {

    private final UUID id;
    private final WebhookEvent event;
    private final ZonedDateTime start;
    @Setter
    ZonedDateTime end;
    @Setter
    Status status;

    public ProcessingStatus(WebhookEvent event) {
        this.id = UUID.randomUUID();
        this.event = event;
        this.start = ZonedDateTime.now();
        this.end = null;
        this.status = Status.STARTED;
    }

    public ProcessingStatus end(boolean sucess) {
        if (sucess) {
            status = Status.SUCCESS;
        } else {
            status = Status.FAILD;
        }
        end = ZonedDateTime.now();
        return this;
    }

    public boolean eligible() {
        return status == Status.FAILD || status == status.STARTED;
    }

    public static enum Status {
        STARTED, FAILD, SUCCESS;
    }
}
