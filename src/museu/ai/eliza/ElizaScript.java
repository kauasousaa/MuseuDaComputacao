package museu.ai.eliza;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import museu.json.JsonFileStore;
import museu.json.JsonObject;

public record ElizaScript(List<ElizaRule> rules, List<String> fallbackResponses, Map<String, String> reflections) {

    public static ElizaScript load(Path filePath) {
        JsonObject json = new JsonFileStore(filePath).read()
                .map(JsonObject::from)
                .orElseThrow(() -> new IllegalStateException("Script da ELIZA não encontrado: " + filePath));
        return new ElizaScript(
                json.getObjectList("rules").stream().map(ElizaRule::fromJson).toList(),
                json.getStringList("fallbackResponses"),
                json.getObject("reflections").toStringMap());
    }
}
