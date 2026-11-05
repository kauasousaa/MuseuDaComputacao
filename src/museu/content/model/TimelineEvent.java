package museu.content.model;

import museu.json.JsonObject;

public record TimelineEvent(int year, String title, String description, String category) {

    public static TimelineEvent fromJson(JsonObject json) {
        return new TimelineEvent(
                json.getInt("year"),
                json.getString("title"),
                json.getString("description"),
                json.getString("category"));
    }
}
