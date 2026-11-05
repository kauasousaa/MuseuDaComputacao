package museu.http;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import museu.json.JsonWriter;

public record HttpResponse(int statusCode, String contentType, byte[] body, Map<String, String> headers) {

    private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

    public static HttpResponse json(int statusCode, Object payload) {
        byte[] body = JsonWriter.write(payload).getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(statusCode, JSON_CONTENT_TYPE, body, Map.of("Cache-Control", "no-store"));
    }

    public static HttpResponse ok(Object payload) {
        return json(HttpStatus.OK, payload);
    }

    public static HttpResponse created(Object payload) {
        return json(HttpStatus.CREATED, payload);
    }

    public static HttpResponse noContent() {
        return new HttpResponse(HttpStatus.NO_CONTENT, JSON_CONTENT_TYPE, new byte[0], Map.of());
    }

    public static HttpResponse error(int statusCode, String message) {
        return json(statusCode, Map.of("error", message));
    }

    public static HttpResponse file(byte[] content, String contentType) {
        return new HttpResponse(HttpStatus.OK, contentType, content, Map.of("Cache-Control", "no-cache"));
    }
}
