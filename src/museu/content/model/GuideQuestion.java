package museu.content.model;

import museu.json.JsonObject;

public record GuideQuestion(String text) {

    public static GuideQuestion fromJson(JsonObject json) {
        return new GuideQuestion(json.getString("text"));
    }
}
