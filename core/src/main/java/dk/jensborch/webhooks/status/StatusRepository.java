package dk.jensborch.webhooks.status;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import dk.jensborch.webhooks.Webhook;

/**
 * Repository for manipulating webhook event statuses.
 */
public interface StatusRepository {

    ProcessingStatus save(@NotNull @Valid ProcessingStatus status);

    Optional<ProcessingStatus> find(@NotNull UUID eventId);

    SortedSet<ProcessingStatus> list(@NotNull ZonedDateTime from, @NotNull String... topic);

    SortedSet<ProcessingStatus> list(@NotNull ZonedDateTime from, Webhook webhook);

}
