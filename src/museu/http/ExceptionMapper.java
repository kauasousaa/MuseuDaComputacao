package museu.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import museu.json.JsonParseException;
import museu.shared.exception.ForbiddenException;
import museu.shared.exception.NotFoundException;
import museu.shared.exception.PayloadTooLargeException;
import museu.shared.exception.RateLimitExceededException;
import museu.shared.exception.ValidationException;

public final class ExceptionMapper {

    private static final Logger LOGGER = Logger.getLogger(ExceptionMapper.class.getName());

    private final Map<Class<? extends Exception>, Integer> statusByException = new LinkedHashMap<>();

    public ExceptionMapper() {
        statusByException.put(ValidationException.class, HttpStatus.BAD_REQUEST);
        statusByException.put(JsonParseException.class, HttpStatus.BAD_REQUEST);
        statusByException.put(ForbiddenException.class, HttpStatus.FORBIDDEN);
        statusByException.put(NotFoundException.class, HttpStatus.NOT_FOUND);
        statusByException.put(PayloadTooLargeException.class, HttpStatus.PAYLOAD_TOO_LARGE);
        statusByException.put(RateLimitExceededException.class, HttpStatus.TOO_MANY_REQUESTS);
    }

    public HttpResponse toResponse(Exception exception) {
        return statusByException.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(exception))
                .findFirst()
                .map(entry -> HttpResponse.error(entry.getValue(), exception.getMessage()))
                .orElseGet(() -> unexpectedError(exception));
    }

    private HttpResponse unexpectedError(Exception exception) {
        LOGGER.log(Level.SEVERE, "Erro inesperado ao processar requisição", exception);
        return HttpResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno no servidor do museu.");
    }
}
