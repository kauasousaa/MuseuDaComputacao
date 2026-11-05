package museu.timeline;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import museu.content.ContentRepository;
import museu.content.model.TimelineEvent;

public final class TimelineService {

    private final ContentRepository<TimelineEvent> repository;

    public TimelineService(ContentRepository<TimelineEvent> repository) {
        this.repository = repository;
    }

    public List<TimelineEvent> findEvents(Optional<String> category) {
        return repository.findAll().stream()
                .filter(event -> category.map(event.category()::equalsIgnoreCase).orElse(true))
                .sorted(Comparator.comparingInt(TimelineEvent::year))
                .toList();
    }
}
