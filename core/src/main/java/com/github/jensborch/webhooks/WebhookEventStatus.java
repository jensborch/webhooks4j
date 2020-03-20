package com.github.jensborch.webhooks;

import java.beans.ConstructorProperties;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Processing status for a webhook event.
 */
public class WebhookEventStatus implements Comparable<WebhookEventStatus> {

    private ZonedDateTime end;

    @NotNull
    private Status status;

    @NotNull
    private final UUID id;

    @NotNull
    @Valid
    private final WebhookEvent event;

    @NotNull
    private final ZonedDateTime start;

    @ConstructorProperties({"id", "event", "start", "end", "status"})
    protected WebhookEventStatus(final UUID id, final WebhookEvent event, final ZonedDateTime start, final ZonedDateTime end, final Status status) {
        if (!id.equals(event.getId())) {
            throw new IllegalArgumentException("Status ID and event ID must be identical");
        }
        this.end = end;
        this.status = status;
        this.id = id;
        this.event = event;
        this.start = start;
    }

    public WebhookEventStatus(final WebhookEvent event) {
        this(event.getId(), event, ZonedDateTime.now(), null, Status.STARTED);
    }

    public void setEnd(final ZonedDateTime end) {
        this.end = end;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    public Status getStatus() {
        return status;
    }

    public UUID getId() {
        return id;
    }

    public WebhookEvent getEvent() {
        return event;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    @Override
    public String toString() {
        return "WebhookEventStatus{" + "end=" + end + ", status=" + status + ", id=" + id + ", event=" + event + ", start=" + start + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.end);
        hash = 89 * hash + Objects.hashCode(this.status);
        hash = 89 * hash + Objects.hashCode(this.id);
        hash = 89 * hash + Objects.hashCode(this.event);
        hash = 89 * hash + Objects.hashCode(this.start);
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
        final WebhookEventStatus other = (WebhookEventStatus) obj;
        return Objects.equals(this.end, other.end)
                && this.status == other.status
                && Objects.equals(this.id, other.id)
                && Objects.equals(this.event, other.event)
                && Objects.equals(this.start, other.start);
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
        return status != Status.SUCCESS;
    }

    @Override
    public int compareTo(final WebhookEventStatus other) {
        Objects.requireNonNull(other, "WebhookEventStatus can not be null");
        return Comparator
                .comparing(WebhookEventStatus::getStart)
                .reversed()
                .thenComparing(WebhookEventStatus::getId)
                .compare(this, other);
    }

    /**
     * The processing status of the event.
     */
    public enum Status {
        STARTED, FAILED, SUCCESS
    }
}
