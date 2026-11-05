package museu.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private static final String API_PREFIX = "/api/";
    private static final int MAX_BODY_BYTES = 64 * 1024;
    private static final int THREAD_POOL_SIZE = 16;
    private static final Map<String, String> SECURITY_HEADERS = Map.of(
            "X-Content-Type-Options", "nosniff",
            "X-Frame-Options", "DENY",
            "Referrer-Policy", "no-referrer");

    private final Router router;
    private final RouteHandler staticFileHandler;
    private final ExceptionMapper exceptionMapper;
    private final HttpRequestFactory requestFactory = new HttpRequestFactory(MAX_BODY_BYTES);
    private HttpServer httpServer;
    private ExecutorService executor;

    public WebServer(Router router, RouteHandler staticFileHandler, ExceptionMapper exceptionMapper) {
        this.router = router;
        this.staticFileHandler = staticFileHandler;
        this.exceptionMapper = exceptionMapper;
    }

    public void start(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        httpServer.createContext("/", this::handleExchange);
        httpServer.setExecutor(executor);
        httpServer.start();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            executor.shutdown();
        }
    }

    private void handleExchange(HttpExchange exchange) throws IOException {
        long startedAt = System.nanoTime();
        try {
            HttpResponse response = processSafely(exchange);
            writeResponse(exchange, response);
            logRequest(exchange, response.statusCode(), startedAt);
        } finally {
            exchange.close();
        }
    }

    private HttpResponse processSafely(HttpExchange exchange) {
        try {
            return process(exchange);
        } catch (Exception exception) {
            return exceptionMapper.toResponse(exception);
        }
    }

    private HttpResponse process(HttpExchange exchange) throws IOException {
        Optional<HttpMethod> method = HttpMethod.parse(exchange.getRequestMethod());
        if (method.isEmpty()) {
            return HttpResponse.error(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado.");
        }
        HttpRequest request = requestFactory.create(exchange, method.get());
        return request.path().startsWith(API_PREFIX) ? routeApiRequest(request) : serveStaticFile(request);
    }

    private HttpResponse routeApiRequest(HttpRequest request) {
        return router.match(request.method(), request.path())
                .map(match -> match.handler().handle(request.withPathParameters(match.pathParameters())))
                .orElseGet(() -> HttpResponse.error(HttpStatus.NOT_FOUND, "Rota da API não encontrada."));
    }

    private HttpResponse serveStaticFile(HttpRequest request) {
        boolean isReadMethod = request.method() == HttpMethod.GET || request.method() == HttpMethod.HEAD;
        if (!isReadMethod) {
            return HttpResponse.error(HttpStatus.METHOD_NOT_ALLOWED, "Arquivos estáticos aceitam apenas GET.");
        }
        return staticFileHandler.handle(request);
    }

    private void writeResponse(HttpExchange exchange, HttpResponse response) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", response.contentType());
        SECURITY_HEADERS.forEach(headers::set);
        response.headers().forEach(headers::set);

        byte[] body = response.body();
        boolean isHeadRequest = HttpMethod.HEAD.name().equalsIgnoreCase(exchange.getRequestMethod());
        boolean hasBody = body.length > 0 && !isHeadRequest && response.statusCode() != HttpStatus.NO_CONTENT;
        exchange.sendResponseHeaders(response.statusCode(), hasBody ? body.length : -1);
        if (hasBody) {
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
        }
    }

    private static void logRequest(HttpExchange exchange, int statusCode, long startedAt) {
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
        LOGGER.info(() -> String.format("%s %s -> %d (%d ms)",
                exchange.getRequestMethod(), exchange.getRequestURI().getPath(), statusCode, elapsedMillis));
    }
}
