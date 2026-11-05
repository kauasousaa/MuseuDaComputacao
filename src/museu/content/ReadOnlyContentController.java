package museu.content;

import museu.http.Controller;
import museu.http.HttpResponse;
import museu.http.Router;

public final class ReadOnlyContentController<T> implements Controller {

    private final String path;
    private final ContentRepository<T> repository;

    public ReadOnlyContentController(String path, ContentRepository<T> repository) {
        this.path = path;
        this.repository = repository;
    }

    @Override
    public void registerRoutes(Router router) {
        router.get(path, request -> HttpResponse.ok(repository.findAll()));
    }
}
