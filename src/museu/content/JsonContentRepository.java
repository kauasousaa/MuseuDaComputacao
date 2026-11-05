package museu.content;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import museu.json.JsonFileStore;
import museu.json.JsonObject;

public final class JsonContentRepository<T> implements ContentRepository<T> {

    private final List<T> items;

    public JsonContentRepository(Path filePath, Function<JsonObject, T> mapper) {
        Object root = new JsonFileStore(filePath).read()
                .orElseThrow(() -> new IllegalStateException("Arquivo de conteúdo não encontrado: " + filePath));
        this.items = JsonObject.listFrom(root).stream().map(mapper).toList();
    }

    @Override
    public List<T> findAll() {
        return items;
    }
}
