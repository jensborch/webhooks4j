package dk.jensborch.webhooks.status;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 */
public interface StatusRepository {

    ProcessingStatus save(@NotNull @Valid ProcessingStatus status);

    Optional<ProcessingStatus> find(@NotNull UUID id);

    Optional<ProcessingStatus> findByEventId(@NotNull UUID eventId);

    Set<ProcessingStatus> list(@NotNull ZonedDateTime from, @NotNull @Size(min = 1) String... topic);

}
