package museu.memory;

import museu.json.JsonObject;

public record NewMemoryRequest(String text, String eraCode, String authorName, String guideQuestion) {

    public static NewMemoryRequest fromJson(JsonObject json) {
        return new NewMemoryRequest(
                json.getStringOrDefault("text", ""),
                json.getStringOrDefault("era", ""),
                json.getStringOrDefault("authorName", ""),
                json.getStringOrDefault("guideQuestion", ""));
    }
}
