package museu.era;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import museu.shared.exception.RateLimitExceededException;
import museu.shared.security.RateLimiter;

public final class EraVoteService {

    private final EraVoteRepository repository;
    private final RateLimiter rateLimiter;

    public EraVoteService(EraVoteRepository repository, RateLimiter rateLimiter) {
        this.repository = repository;
        this.rateLimiter = rateLimiter;
    }

    public List<EraVoteCount> summarize() {
        Map<Era, Integer> votes = repository.countAll();
        return Arrays.stream(Era.values())
                .map(era -> new EraVoteCount(era, votes.getOrDefault(era, 0)))
                .toList();
    }

    public List<EraVoteCount> vote(String eraCode, String clientKey) {
        Era era = Era.parse(eraCode);
        if (!rateLimiter.tryAcquire(clientKey)) {
            throw new RateLimitExceededException("Muitos votos seguidos. Aguarde um instante.");
        }
        repository.increment(era);
        return summarize();
    }
}
