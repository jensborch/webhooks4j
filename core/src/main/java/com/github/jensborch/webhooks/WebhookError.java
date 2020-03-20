package com.github.jensborch.webhooks;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of an error message returned by the API compatible with
 * <a href="https://tools.ietf.org/html/rfc7807">RFC7807</a>
 */
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

    @NotNull
    private final Integer status;

    @NotNull
    private final Code code;

    @NotNull
    private final String title;

    @NotNull
    private final String detail;

    @ConstructorProperties({"status", "code", "title", "details"})
    protected WebhookError(final Integer status, final Code code, final String title, final String detail) {
        this.status = status;
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    public WebhookError(final int status, final Code code, final String detail) {
        this(status, code, code.getTitle(), detail);
    }

    public WebhookError(final Code code, final String msg) {
        this(code.getStatus().getStatusCode(), code, msg);
    }

    public WebhookError(final Integer status, final String msg) {
        this(status, HTTP_STATUS_MAP.getOrDefault(Response.Status.fromStatusCode(status), Code.UNKNOWN_ERROR), msg);
    }

    public Integer getStatus() {
        return status;
    }

    public Code getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "WebhookError{" + "status=" + status + ", code=" + code + ", title=" + title + ", detail=" + detail + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.status);
        hash = 83 * hash + Objects.hashCode(this.code);
        hash = 83 * hash + Objects.hashCode(this.title);
        hash = 83 * hash + Objects.hashCode(this.detail);
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
        final WebhookError other = (WebhookError) obj;
        return Objects.equals(this.title, other.title)
                && Objects.equals(this.detail, other.detail)
                && Objects.equals(this.status, other.status)
                && this.code == other.code;
    }

    @SuppressWarnings("PMD")
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public static WebhookError parse(final Response response) {
        if (response.hasEntity()) {
            try {
                String data = response.readEntity(String.class);
                try (JsonReader reader = Json.createReader(new StringReader(data))) {
                    JsonObject json = reader.readObject();
                    return new WebhookError(
                            response.getStatus(),
                            Code.fromString(json.getString("code")),
                            json.getString("msg", "Message is undefined"));
                } catch (JsonParsingException e) {
                    LOG.warn("Unable to parse error response as JSON", e);
                    return new WebhookError(response.getStatus(), data);
                }
            } catch (ProcessingException e) {
                LOG.warn("Unable to parse error response as string", e);
                return new WebhookError(response.getStatus(), e.getMessage());
            } catch (RuntimeException e) {
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
    public enum Code {
        AUTHORIZATION_ERROR(Response.Status.UNAUTHORIZED, "User not authorization"),
        AUTHENTICATION_ERROR(Response.Status.FORBIDDEN, "User not authenticated"),
        VALIDATION_ERROR(Response.Status.BAD_REQUEST, "Validation error"),
        UNKNOWN_PUBLISHER(Response.Status.BAD_REQUEST, "Unknown publisher"),
        UNKNOWN_ERROR(Response.Status.INTERNAL_SERVER_ERROR, "Unknown error"),
        REGISTER_ERROR(Response.Status.SERVICE_UNAVAILABLE, "Webhook registration error"),
        NOT_FOUND(Response.Status.NOT_FOUND, "Not found"),
        SYNC_ERROR(Response.Status.SERVICE_UNAVAILABLE, "Event synchronisation error"),
        ILLEGAL_STATUS(Response.Status.BAD_REQUEST, "Illegal webhook state");

        private final Response.Status status;

        private final String title;

        Code(final Response.Status status, final String title) {
            this.status = status;
            this.title = title;
        }

        public Response.Status getStatus() {
            return status;
        }

        public String getTitle() {
            return title;
        }

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
