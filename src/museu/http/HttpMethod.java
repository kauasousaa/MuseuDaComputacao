package museu.http;

import java.util.Arrays;
import java.util.Optional;

public enum HttpMethod {
    GET, HEAD, POST, PUT, DELETE;

    public static Optional<HttpMethod> parse(String value) {
        return Arrays.stream(values())
                .filter(method -> method.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
