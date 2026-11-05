package museu.timeline;

import museu.http.Controller;
import museu.http.HttpRequest;
import museu.http.HttpResponse;
import museu.http.Router;

public final class TimelineController implements Controller {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @Override
    public void registerRoutes(Router router) {
        router.get("/api/timeline", this::listEvents);
    }

    private HttpResponse listEvents(HttpRequest request) {
        return HttpResponse.ok(timelineService.findEvents(request.queryParameter("category")));
    }
}
