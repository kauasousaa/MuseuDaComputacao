package museu.ai.eliza;

import java.util.List;
import java.util.regex.Pattern;
import museu.json.JsonObject;

public record ElizaRule(Pattern pattern, List<String> responses) {

    public static ElizaRule fromJson(JsonObject json) {
        Pattern pattern = Pattern.compile(json.getString("pattern"),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.UNICODE_CHARACTER_CLASS);
        return new ElizaRule(pattern, json.getStringList("responses"));
    }
}
