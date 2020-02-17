package dk.jensborch.webhooks;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    public Optional<ProcessingStatus> find(final UUID id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public Set<ProcessingStatus> list(final ZonedDateTime from, final String... topic) {
        return topic != null && topic.length > 0
                ? map.values().stream()
                        .filter(p -> p.getStart().isAfter(from))
                        .filter(p -> Arrays.binarySearch(topic, p.getEvent().getTopic()) >= 0)
                        .collect(Collectors.toSet())
                : map.values().stream().collect(Collectors.toSet());
    }

}
