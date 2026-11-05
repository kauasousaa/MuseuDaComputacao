package museu.memory;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import museu.era.Era;
import museu.shared.exception.NotFoundException;
import museu.shared.exception.RateLimitExceededException;
import museu.shared.security.AdminAuthorizer;
import museu.shared.security.RateLimiter;

public final class MemoryService {

    private static final int MAX_LISTED_MEMORIES = 200;

    private final MemoryRepository repository;
    private final MemoryValidator validator;
    private final RateLimiter rateLimiter;
    private final AdminAuthorizer adminAuthorizer;
    private final Clock clock;

    public MemoryService(
            MemoryRepository repository,
            MemoryValidator validator,
            RateLimiter rateLimiter,
            AdminAuthorizer adminAuthorizer,
            Clock clock) {
        this.repository = repository;
        this.validator = validator;
        this.rateLimiter = rateLimiter;
        this.adminAuthorizer = adminAuthorizer;
        this.clock = clock;
    }

    public List<MemoryEntry> listRecent(Optional<Era> eraFilter) {
        return repository.findAll().stream()
                .filter(entry -> eraFilter.map(era -> entry.era() == era).orElse(true))
                .sorted(Comparator.comparing(MemoryEntry::createdAt).reversed())
                .limit(MAX_LISTED_MEMORIES)
                .toList();
    }

    public MemoryEntry create(NewMemoryRequest request, String clientKey) {
        MemoryDraft draft = validator.validate(request);
        if (!rateLimiter.tryAcquire(clientKey)) {
            throw new RateLimitExceededException("Calma! Aguarde um pouco antes de colar outra memória no mural.");
        }
        MemoryEntry entry = new MemoryEntry(
                UUID.randomUUID().toString(),
                draft.text(),
                draft.era(),
                draft.authorName(),
                draft.guideQuestion(),
                clock.instant());
        repository.save(entry);
        return entry;
    }

    public void delete(String id, String adminKey) {
        adminAuthorizer.authorize(adminKey);
        if (!repository.deleteById(id)) {
            throw new NotFoundException("Memória não encontrada.");
        }
    }
}
