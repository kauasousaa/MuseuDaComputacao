package museu.memory;

import museu.era.Era;
import museu.http.Controller;
import museu.http.HttpRequest;
import museu.http.HttpResponse;
import museu.http.Router;

public final class MemoryController implements Controller {

    private static final String ADMIN_KEY_HEADER = "X-Admin-Key";

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @Override
    public void registerRoutes(Router router) {
        router.get("/api/memories", this::listMemories)
                .post("/api/memories", this::createMemory)
                .delete("/api/memories/{id}", this::deleteMemory);
    }

    private HttpResponse listMemories(HttpRequest request) {
        return HttpResponse.ok(memoryService.listRecent(request.queryParameter("era").map(Era::parse)));
    }

    private HttpResponse createMemory(HttpRequest request) {
        NewMemoryRequest newMemory = NewMemoryRequest.fromJson(request.jsonBody());
        return HttpResponse.created(memoryService.create(newMemory, request.clientAddress()));
    }

    private HttpResponse deleteMemory(HttpRequest request) {
        memoryService.delete(request.pathParameter("id"), request.header(ADMIN_KEY_HEADER).orElse(""));
        return HttpResponse.noContent();
    }
}
