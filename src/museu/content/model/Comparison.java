package museu.content.model;

import museu.json.JsonObject;

public record Comparison(String theme, String past, String pastPeriod, String present, String curiosity) {

    public static Comparison fromJson(JsonObject json) {
        return new Comparison(
                json.getString("theme"),
                json.getString("past"),
                json.getString("pastPeriod"),
                json.getString("present"),
                json.getString("curiosity"));
    }
}
