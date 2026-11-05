package museu.http;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import museu.json.JsonObject;
import museu.json.JsonParser;

public record HttpRequest(
        HttpMethod method,
        String path,
        Map<String, String> queryParameters,
        Map<String, String> pathParameters,
        Map<String, String> headers,
        String body,
        String clientAddress) {

    public Optional<String> queryParameter(String name) {
        return Optional.ofNullable(queryParameters.get(name)).filter(value -> !value.isBlank());
    }

    public String pathParameter(String name) {
        return pathParameters.getOrDefault(name, "");
    }

    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase(Locale.ROOT)));
    }

    public JsonObject jsonBody() {
        return JsonObject.from(JsonParser.parse(body));
    }

    public HttpRequest withPathParameters(Map<String, String> parameters) {
        return new HttpRequest(method, path, queryParameters, Map.copyOf(parameters), headers, body, clientAddress);
    }
}
