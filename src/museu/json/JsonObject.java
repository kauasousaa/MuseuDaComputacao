package museu.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonObject {

    private final Map<String, Object> values;

    private JsonObject(Map<String, Object> values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public static JsonObject from(Object value) {
        if (value instanceof Map<?, ?> map) {
            return new JsonObject((Map<String, Object>) map);
        }
        throw new JsonParseException("Era esperado um objeto JSON.");
    }

    public static List<JsonObject> listFrom(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(JsonObject::from).toList();
        }
        throw new JsonParseException("Era esperada uma lista JSON.");
    }

    public String getString(String key) {
        Object value = values.get(key);
        if (value instanceof String text) {
            return text;
        }
        throw new JsonParseException("O campo '" + key + "' deve ser um texto.");
    }

    public String getStringOrDefault(String key, String defaultValue) {
        Object value = values.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof String text) {
            return text;
        }
        throw new JsonParseException("O campo '" + key + "' deve ser um texto.");
    }

    public int getInt(String key) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new JsonParseException("O campo '" + key + "' deve ser um número.");
    }

    public int getIntOrDefault(String key, int defaultValue) {
        return values.containsKey(key) ? getInt(key) : defaultValue;
    }

    public JsonObject getObject(String key) {
        return from(values.get(key));
    }

    public List<JsonObject> getObjectList(String key) {
        return listFrom(values.get(key));
    }

    public List<String> getStringList(String key) {
        Object value = values.get(key);
        if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            return list.stream().map(String.class::cast).toList();
        }
        throw new JsonParseException("O campo '" + key + "' deve ser uma lista de textos.");
    }

    public Map<String, String> toStringMap() {
        Map<String, String> result = new LinkedHashMap<>();
        values.keySet().forEach(key -> result.put(key, getString(key)));
        return result;
    }
}
