package museu.content.model;

import museu.json.JsonObject;

public record RecognizeChallenge(String id, String snippet, String answer, String period, String explanation) {

    public static RecognizeChallenge fromJson(JsonObject json) {
        return new RecognizeChallenge(
                json.getString("id"),
                json.getString("snippet"),
                json.getString("answer"),
                json.getString("period"),
                json.getString("explanation"));
    }
}
