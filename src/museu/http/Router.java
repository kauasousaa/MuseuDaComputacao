package museu.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Router {

    private final List<Route> routes = new ArrayList<>();

    public Router get(String pattern, RouteHandler handler) {
        return register(HttpMethod.GET, pattern, handler);
    }

    public Router post(String pattern, RouteHandler handler) {
        return register(HttpMethod.POST, pattern, handler);
    }

    public Router delete(String pattern, RouteHandler handler) {
        return register(HttpMethod.DELETE, pattern, handler);
    }

    public Router register(HttpMethod method, String pattern, RouteHandler handler) {
        routes.add(new Route(method, splitPath(pattern), handler));
        return this;
    }

    public Optional<RouteMatch> match(HttpMethod method, String path) {
        String[] pathSegments = splitPath(path);
        for (Route route : routes) {
            if (route.method() != method) {
                continue;
            }
            Optional<Map<String, String>> parameters = route.matchSegments(pathSegments);
            if (parameters.isPresent()) {
                return Optional.of(new RouteMatch(route.handler(), parameters.get()));
            }
        }
        return Optional.empty();
    }

    private static String[] splitPath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    public record RouteMatch(RouteHandler handler, Map<String, String> pathParameters) {
    }

    private record Route(HttpMethod method, String[] segments, RouteHandler handler) {

        Optional<Map<String, String>> matchSegments(String[] pathSegments) {
            if (segments.length != pathSegments.length) {
                return Optional.empty();
            }
            Map<String, String> parameters = new HashMap<>();
            for (int index = 0; index < segments.length; index++) {
                String expected = segments[index];
                if (isParameter(expected)) {
                    parameters.put(expected.substring(1, expected.length() - 1), pathSegments[index]);
                } else if (!expected.equals(pathSegments[index])) {
                    return Optional.empty();
                }
            }
            return Optional.of(parameters);
        }

        private static boolean isParameter(String segment) {
            return segment.startsWith("{") && segment.endsWith("}");
        }
    }
}
