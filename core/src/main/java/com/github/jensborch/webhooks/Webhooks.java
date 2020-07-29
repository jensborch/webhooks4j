package com.github.jensborch.webhooks;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
 * Webhook list.
 */
public class Webhooks {

    @SuppressWarnings("PMD.AvoidFieldNameMatchingTypeName")
    private final SortedSet<Webhook> webhooks;

    protected Webhooks() {
        //Needed by Jackson
        this.webhooks = new TreeSet<>();
    }

    public Webhooks(final Collection<Webhook> webhooks) {
        Objects.requireNonNull(webhooks, "Webhooks should not be null");
        this.webhooks = new TreeSet<>(webhooks);
    }

    public SortedSet<Webhook> getWebhooks() {
        return Collections.unmodifiableSortedSet(webhooks);
    }

    @JsonProperty(access = Access.READ_ONLY)
    public int getSize() {
        return webhooks.size();
    }
}
