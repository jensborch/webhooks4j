package dk.jensborch.webhooks;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * Representation of an error message returned by the API.
 */
@Value
public class WebhookError implements Serializable {

    private static final long serialVersionUID = 8387757018701335705L;

    Code code;
    String msg;

    public static WebhookError parse(final Response response) {
        try {
            return response.hasEntity()
                    ? response.readEntity(WebhookError.class)
                    : new WebhookError(Code.UNKNOWN_ERROR, "No entity");
        } catch (ProcessingException e) {
            return new WebhookError(Code.UNKNOWN_ERROR, e.getMessage());
        }
    }

    public static Map<String, Object> parseErrorResponseToMap(final Response response) {
        try {
            return response.hasEntity()
                    ? response.readEntity(new GenericType<HashMap<String, Object>>() {
                    })
                    : new HashMap<>(0);
        } catch (ProcessingException e) {
            return Collections.singletonMap("msg", e.getMessage());
        }
    }

    public static String parseErrorResponseToString(final Response response) {
        return parseErrorResponseToMap(response).entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Error codes that can be returned by the API.
     */
    @AllArgsConstructor
    public enum Code {
        VALIDATION_ERROR(Response.Status.BAD_REQUEST),
        UNKNOWN_PUBLISHER(Response.Status.BAD_REQUEST),
        UNKNOWN_ERROR(Response.Status.INTERNAL_SERVER_ERROR),
        REGISTER_ERROR(Response.Status.SERVICE_UNAVAILABLE),
        NOT_FOUND(Response.Status.NOT_FOUND),
        SYNC_ERROR(Response.Status.SERVICE_UNAVAILABLE),
        ILLEGAL_STATUS(Response.Status.BAD_REQUEST);

        @Getter
        private final Response.Status status;
    }
}
