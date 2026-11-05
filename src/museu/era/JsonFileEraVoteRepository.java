package museu.era;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import museu.json.JsonFileStore;
import museu.json.JsonObject;

public final class JsonFileEraVoteRepository implements EraVoteRepository {

    private final JsonFileStore fileStore;
    private final EnumMap<Era, Integer> votes = new EnumMap<>(Era.class);

    public JsonFileEraVoteRepository(JsonFileStore fileStore) {
        this.fileStore = fileStore;
        fileStore.read().map(JsonObject::from).ifPresent(this::loadVotes);
    }

    @Override
    public synchronized Map<Era, Integer> countAll() {
        return new EnumMap<>(votes);
    }

    @Override
    public synchronized void increment(Era era) {
        votes.merge(era, 1, Integer::sum);
        persist();
    }

    private void loadVotes(JsonObject json) {
        for (Era era : Era.values()) {
            votes.put(era, json.getIntOrDefault(era.code(), 0));
        }
    }

    private void persist() {
        Map<String, Object> json = new LinkedHashMap<>();
        votes.forEach((era, count) -> json.put(era.code(), count));
        fileStore.write(json);
    }
}
