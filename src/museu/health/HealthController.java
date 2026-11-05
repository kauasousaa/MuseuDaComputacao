package museu.health;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import museu.http.Controller;
import museu.http.HttpResponse;
import museu.http.Router;

public final class HealthController implements Controller {

    private final Clock clock;
    private final Instant startedAt;

    public HealthController(Clock clock) {
        this.clock = clock;
        this.startedAt = clock.instant();
    }

    @Override
    public void registerRoutes(Router router) {
        router.get("/api/health", request -> HttpResponse.ok(Map.of(
                "status", "online",
                "startedAt", startedAt,
                "serverTime", clock.instant())));
    }
}
