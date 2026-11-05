package museu.http;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class StaticFileHandler implements RouteHandler {

    private static final String INDEX_FILE = "index.html";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8",
            "json", "application/json; charset=utf-8",
            "svg", "image/svg+xml",
            "png", "image/png",
            "jpg", "image/jpeg",
            "ico", "image/x-icon",
            "woff2", "font/woff2");

    private final Path webRoot;

    public StaticFileHandler(Path webRoot) {
        this.webRoot = webRoot.toAbsolutePath().normalize();
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        return resolveFile(request.path())
                .map(this::readFile)
                .orElseGet(() -> HttpResponse.error(HttpStatus.NOT_FOUND, "Arquivo não encontrado."));
    }

    private Optional<Path> resolveFile(String requestPath) {
        try {
            Path candidate = webRoot.resolve(requestPath.replaceFirst("^/+", "")).normalize();
            if (!candidate.startsWith(webRoot)) {
                return Optional.empty();
            }
            Path file = Files.isDirectory(candidate) ? candidate.resolve(INDEX_FILE) : candidate;
            return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
        } catch (InvalidPathException exception) {
            return Optional.empty();
        }
    }

    private HttpResponse readFile(Path file) {
        try {
            return HttpResponse.file(Files.readAllBytes(file), contentTypeOf(file));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static String contentTypeOf(Path file) {
        String fileName = file.getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return CONTENT_TYPES.getOrDefault(extension, DEFAULT_CONTENT_TYPE);
    }
}
