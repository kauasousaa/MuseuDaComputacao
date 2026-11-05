package museu.config;

import java.nio.file.Path;
import java.util.Optional;

public record ServerConfig(
        int port,
        Path webRoot,
        Path contentDirectory,
        Path runtimeDirectory,
        Optional<String> adminKey) {

    private static final int DEFAULT_PORT = 8080;

    public static ServerConfig fromEnvironment() {
        Path baseDirectory = Path.of("").toAbsolutePath();
        Path dataDirectory = baseDirectory.resolve("data");
        return new ServerConfig(
                readPort(),
                baseDirectory.resolve("web"),
                dataDirectory.resolve("content"),
                dataDirectory.resolve("runtime"),
                Optional.ofNullable(System.getenv("MUSEU_ADMIN_KEY")));
    }

    private static int readPort() {
        String value = System.getenv("PORT");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(value.strip());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Variável PORT inválida: " + value, exception);
        }
    }
}
