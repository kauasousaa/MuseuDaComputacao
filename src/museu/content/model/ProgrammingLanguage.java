package museu.content.model;

import museu.json.JsonObject;

public record ProgrammingLanguage(
        String id,
        String name,
        int year,
        String creator,
        String paradigm,
        String summary,
        String fileName,
        String theme,
        String runtime,
        String sampleCode,
        String sampleOutput) {

    public static ProgrammingLanguage fromJson(JsonObject json) {
        return new ProgrammingLanguage(
                json.getString("id"),
                json.getString("name"),
                json.getInt("year"),
                json.getString("creator"),
                json.getString("paradigm"),
                json.getString("summary"),
                json.getString("fileName"),
                json.getString("theme"),
                json.getString("runtime"),
                json.getString("sampleCode"),
                json.getString("sampleOutput"));
    }
}
