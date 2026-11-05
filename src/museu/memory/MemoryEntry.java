package museu.memory;

import java.time.Instant;
import museu.era.Era;
import museu.json.JsonObject;

public record MemoryEntry(
        String id,
        String text,
        Era era,
        String authorName,
        String guideQuestion,
        Instant createdAt) {

    public static MemoryEntry fromJson(JsonObject json) {
        return new MemoryEntry(
                json.getString("id"),
                json.getString("text"),
                Era.parse(json.getObject("era").getString("code")),
                json.getStringOrDefault("authorName", null),
                json.getStringOrDefault("guideQuestion", null),
                Instant.parse(json.getString("createdAt")));
    }
}
