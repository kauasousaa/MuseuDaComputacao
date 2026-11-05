package museu.memory;

import java.util.ArrayList;
import java.util.List;
import museu.json.JsonFileStore;
import museu.json.JsonObject;

public final class JsonFileMemoryRepository implements MemoryRepository {

    private final JsonFileStore fileStore;
    private final List<MemoryEntry> entries;

    public JsonFileMemoryRepository(JsonFileStore fileStore) {
        this.fileStore = fileStore;
        this.entries = new ArrayList<>(loadEntries());
    }

    @Override
    public synchronized List<MemoryEntry> findAll() {
        return List.copyOf(entries);
    }

    @Override
    public synchronized void save(MemoryEntry entry) {
        entries.add(entry);
        fileStore.write(entries);
    }

    @Override
    public synchronized boolean deleteById(String id) {
        boolean removed = entries.removeIf(entry -> entry.id().equals(id));
        if (removed) {
            fileStore.write(entries);
        }
        return removed;
    }

    private List<MemoryEntry> loadEntries() {
        return fileStore.read()
                .map(JsonObject::listFrom)
                .orElse(List.of())
                .stream()
                .map(MemoryEntry::fromJson)
                .toList();
    }
}
