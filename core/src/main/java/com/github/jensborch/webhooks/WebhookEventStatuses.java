package com.github.jensborch.webhooks;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Events status list.
 */
public class WebhookEventStatuses {

    private final SortedSet<WebhookEventStatus> statuses;

    @SuppressWarnings("PMD.NullAssignment")
    protected WebhookEventStatuses() {
        //Needed by Jackson
        this.statuses = null;
    }

    public WebhookEventStatuses(final SortedSet<WebhookEventStatus> statuses) {
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
