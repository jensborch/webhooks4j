package dk.jensborch.webhooks;

import lombok.Getter;

/**
 * Thrown when a error occurs.
 */
public class WebhookException extends RuntimeException {

    private static final long serialVersionUID = 2183253219998476280L;

    @Getter
    private final WebhookError error;

    public WebhookException(final WebhookError error, final Throwable cause) {
        super(cause);
        this.error = error;
    }

    public WebhookException(final WebhookError error) {
        super();
        this.error = error;
    }

}
