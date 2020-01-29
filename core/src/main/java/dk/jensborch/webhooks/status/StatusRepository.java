package dk.jensborch.webhooks.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.Dependent;

/**
 *
 */
@Dependent
public interface StatusRepository {

    ProcessingStatus save(ProcessingStatus status);

    Optional<ProcessingStatus> find(UUID id);

    Optional<ProcessingStatus> findByEventId(UUID eventId);

    Set<ProcessingStatus> list(String... topic);

}
