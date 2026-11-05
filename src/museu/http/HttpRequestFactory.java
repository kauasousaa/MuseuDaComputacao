package museu.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import museu.shared.exception.PayloadTooLargeException;

final class HttpRequestFactory {

    private final int maxBodyBytes;

    HttpRequestFactory(int maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
    }

    HttpRequest create(HttpExchange exchange, HttpMethod method) throws IOException {
        return new HttpRequest(
                method,
                normalizePath(exchange.getRequestURI().getPath()),
                parseQuery(exchange.getRequestURI().getRawQuery()),
                Map.of(),
                readHeaders(exchange),
                readBody(exchange),
                exchange.getRemoteAddress().getAddress().getHostAddress());
    }

    private static String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> parameters = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return parameters;
        }
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            parameters.putIfAbsent(key, value);
        }
        return parameters;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static Map<String, String> readHeaders(HttpExchange exchange) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
            if (!entry.getValue().isEmpty()) {
                headers.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().get(0));
            }
        }
        return headers;
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream input = exchange.getRequestBody()) {
            byte[] content = input.readNBytes(maxBodyBytes + 1);
            if (content.length > maxBodyBytes) {
                throw new PayloadTooLargeException("O corpo da requisição excede " + maxBodyBytes / 1024 + " KB.");
            }
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
