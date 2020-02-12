package dk.jensborch.webhooks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static Map<String, Object> parseErrorResponse(final Response response) {
        return response.readEntity(new GenericType<HashMap<String, Object>>() {
        });
    }

    public static String parseErrorResponseToString(final Response response) {
        return parseErrorResponse(response).entrySet().stream()
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
        REGISTRE_ERROR(Response.Status.SERVICE_UNAVAILABLE),
        NOT_FOUND(Response.Status.NOT_FOUND);

        @Getter
        private final Response.Status status;
    }
}
