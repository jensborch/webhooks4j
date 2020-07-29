package com.github.jensborch.webhooks;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Events status list.
 */
public class WebhookEventStatuses {

    private final SortedSet<WebhookEventStatus> statuses;

    protected WebhookEventStatuses() {
        //Needed by Jackson
        this.statuses = new TreeSet<>();
    }

    public WebhookEventStatuses(final Collection<WebhookEventStatus> statuses) {
        Objects.requireNonNull(statuses, "Statuses should not be null");
        this.statuses = new TreeSet<>(statuses);
    }

    public SortedSet<WebhookEventStatus> getStatuses() {
        return Collections.unmodifiableSortedSet(statuses);
    }

    @JsonProperty(access = Access.READ_ONLY)
    public int getSize() {
        return statuses.size();
    }
}
