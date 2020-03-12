package dk.jensborch.webhooks;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of an error message returned by the API.
 *
 * https://tools.ietf.org/html/rfc7807
 *
 */
@Value
public class WebhookError implements Serializable {

    private static final long serialVersionUID = 8387757018701335705L;
    private static final Logger LOG = LoggerFactory.getLogger(WebhookError.class);

    private static final Map<Response.Status, Code> HTTP_STATUS_MAP = new EnumMap<>(Response.Status.class);

    static {
        HTTP_STATUS_MAP.put(Response.Status.BAD_REQUEST, Code.UNKNOWN_ERROR);
        HTTP_STATUS_MAP.put(Response.Status.UNAUTHORIZED, Code.AUTHENTICATION_ERROR);
        HTTP_STATUS_MAP.put(Response.Status.FORBIDDEN, Code.AUTHORIZATION_ERROR);
        HTTP_STATUS_MAP.put(Response.Status.NOT_FOUND, Code.UNKNOWN_ERROR);
    }

    Integer status;
    Code code;
    String msg;

    public WebhookError(final int status, final Code code, final String msg) {
        this.status = status;
        this.code = code;
        this.msg = msg;
    }

    public WebhookError(final Code code, final String msg) {
        this.status = code.getStatus().getStatusCode();
        this.code = code;
        this.msg = msg;
    }

    public WebhookError(final Integer status, final String msg) {
        this.status = status;
        this.code = HTTP_STATUS_MAP.getOrDefault(Response.Status.fromStatusCode(status), Code.UNKNOWN_ERROR);
        this.msg = msg;
    }

    @SuppressWarnings("PMD")
    public static WebhookError parse(final Response response) {
        if (response.hasEntity()) {
            try {
                String data = response.readEntity(String.class);
                JsonReader reader = Json.createReader(new StringReader(data));
                try {
                    JsonObject json = reader.readObject();
                    return new WebhookError(
                            response.getStatus(),
                            Code.fromString(json.getString("code")),
                            json.getString("msg", "Message is undefined"));
                } catch (JsonParsingException e) {
                    LOG.warn("Unable to parse error response as JSON", e);
                    return new WebhookError(response.getStatus(), data);
                } finally {
                    reader.close();
                }
            } catch (ProcessingException e) {
                LOG.warn("Unable to parse error response as string", e);
                return new WebhookError(response.getStatus(), e.getMessage());
            } catch (Exception e) {
                LOG.warn("Unexpected error", e);
                return new WebhookError(response.getStatus(), e.getMessage());
            }
        } else {
            LOG.info("No entity in response for HTTP status code {}", response.getStatus());
            return new WebhookError(response.getStatus(), "No entity");
        }
    }

    /**
     * Error codes that can be returned by the API.
     */
    @AllArgsConstructor
    public enum Code {
        AUTHORIZATION_ERROR(Response.Status.UNAUTHORIZED),
        AUTHENTICATION_ERROR(Response.Status.FORBIDDEN),
        VALIDATION_ERROR(Response.Status.BAD_REQUEST),
        UNKNOWN_PUBLISHER(Response.Status.BAD_REQUEST),
        UNKNOWN_ERROR(Response.Status.INTERNAL_SERVER_ERROR),
        REGISTER_ERROR(Response.Status.SERVICE_UNAVAILABLE),
        NOT_FOUND(Response.Status.NOT_FOUND),
        SYNC_ERROR(Response.Status.SERVICE_UNAVAILABLE),
        ILLEGAL_STATUS(Response.Status.BAD_REQUEST);

        @Getter
        private final Response.Status status;

        public static Code fromString(final String value) {
            return value == null || value.isEmpty()
                    ? Code.UNKNOWN_ERROR
                    : Arrays.stream(Code.values())
                            .filter(c -> c.name().equalsIgnoreCase(value))
                            .findAny()
                            .orElse(UNKNOWN_ERROR);

        }
    }
}
