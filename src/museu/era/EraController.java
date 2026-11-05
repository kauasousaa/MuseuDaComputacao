package museu.era;

import java.util.List;
import museu.http.Controller;
import museu.http.HttpRequest;
import museu.http.HttpResponse;
import museu.http.Router;

public final class EraController implements Controller {

    private final EraVoteService eraVoteService;

    public EraController(EraVoteService eraVoteService) {
        this.eraVoteService = eraVoteService;
    }

    @Override
    public void registerRoutes(Router router) {
        router.get("/api/eras", request -> HttpResponse.ok(List.of(Era.values())))
                .get("/api/era-votes", request -> HttpResponse.ok(eraVoteService.summarize()))
                .post("/api/era-votes", this::vote);
    }

    private HttpResponse vote(HttpRequest request) {
        String eraCode = request.jsonBody().getString("era");
        return HttpResponse.ok(eraVoteService.vote(eraCode, request.clientAddress()));
    }
}
