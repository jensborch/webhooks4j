package dk.jensborch.webhooks;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dk.jensborch.webhooks.status.ProcessingStatus;
import dk.jensborch.webhooks.status.StatusRepository;

/**
 *
 */
public abstract class HashMapStatusRepository implements StatusRepository {

    private final ConcurrentHashMap<UUID, ProcessingStatus> map = new ConcurrentHashMap<>();

    @Override
    public ProcessingStatus save(final ProcessingStatus status) {
        map.put(status.getId(), status);
        return status;
    }

    @Override
    public ProcessingStatus get(final UUID id) {
        return map.get(id);
    }

    @Override
    public Optional<ProcessingStatus> find(final UUID eventId) {
        return map.entrySet().stream()
                .map(e -> e.getValue()).filter(p -> p.getEvent().getId().equals(eventId))
                .findAny();
    }

}
