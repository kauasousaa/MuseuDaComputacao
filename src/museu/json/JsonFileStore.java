package museu.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public final class JsonFileStore {

    private final Path filePath;

    public JsonFileStore(Path filePath) {
        this.filePath = filePath;
    }

    public Optional<Object> read() {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            return content.isBlank() ? Optional.empty() : Optional.ofNullable(JsonParser.parse(content));
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao ler " + filePath, exception);
        }
    }

    public synchronized void write(Object value) {
        try {
            Files.createDirectories(filePath.getParent());
            Path temporaryFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            Files.writeString(temporaryFile, JsonWriter.write(value), StandardCharsets.UTF_8);
            replaceTarget(temporaryFile);
        } catch (IOException exception) {
            throw new UncheckedIOException("Falha ao gravar " + filePath, exception);
        }
    }

    private void replaceTarget(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
