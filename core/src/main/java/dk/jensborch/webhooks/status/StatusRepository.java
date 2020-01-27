package dk.jensborch.webhooks.status;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;

/**
 *
 */
@Dependent
public interface StatusRepository {

    ProcessingStatus save(ProcessingStatus status);

    Optional<ProcessingStatus> get(UUID id);

    Optional<ProcessingStatus> find(UUID eventId);

}
