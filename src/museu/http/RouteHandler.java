package museu.http;

@FunctionalInterface
public interface RouteHandler {

    HttpResponse handle(HttpRequest request);
}
