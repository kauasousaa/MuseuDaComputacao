package museu.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public final class JsonWriter {

    private JsonWriter() {
    }

    public static String write(Object value) {
        StringBuilder builder = new StringBuilder();
        appendValue(builder, value);
        return builder.toString();
    }

    private static void appendValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof JsonSerializable serializable) {
            appendObject(builder, serializable.toJson());
        } else if (value instanceof String text) {
            appendString(builder, text);
        } else if (value instanceof Number number) {
            appendNumber(builder, number);
        } else if (value instanceof Boolean bool) {
            builder.append(bool);
        } else if (value instanceof Map<?, ?> map) {
            appendObject(builder, map);
        } else if (value instanceof Collection<?> collection) {
            appendArray(builder, collection);
        } else if (value instanceof Record record) {
            appendRecord(builder, record);
        } else if (value instanceof Enum<?> enumValue) {
            appendString(builder, enumValue.name());
        } else if (value instanceof TemporalAccessor temporal) {
            appendString(builder, temporal.toString());
        } else {
            appendString(builder, value.toString());
        }
    }

    private static void appendNumber(StringBuilder builder, Number number) {
        double asDouble = number.doubleValue();
        if (Double.isNaN(asDouble) || Double.isInfinite(asDouble)) {
            builder.append("null");
        } else if ((number instanceof Double || number instanceof Float) && asDouble == Math.rint(asDouble)) {
            builder.append((long) asDouble);
        } else {
            builder.append(number);
        }
    }

    private static void appendObject(StringBuilder builder, Map<?, ?> map) {
        builder.append('{');
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            appendString(builder, String.valueOf(entry.getKey()));
            builder.append(':');
            appendValue(builder, entry.getValue());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append('}');
    }

    private static void appendArray(StringBuilder builder, Collection<?> collection) {
        builder.append('[');
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext()) {
            appendValue(builder, iterator.next());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
    }

    private static void appendRecord(StringBuilder builder, Record record) {
        builder.append('{');
        RecordComponent[] components = record.getClass().getRecordComponents();
        for (int index = 0; index < components.length; index++) {
            appendString(builder, components[index].getName());
            builder.append(':');
            appendValue(builder, readComponent(record, components[index]));
            if (index < components.length - 1) {
                builder.append(',');
            }
        }
        builder.append('}');
    }

    private static Object readComponent(Record record, RecordComponent component) {
        try {
            var accessor = component.getAccessor();
            accessor.setAccessible(true);
            return accessor.invoke(record);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Não foi possível serializar " + component.getName(), exception);
        }
    }

    private static void appendString(StringBuilder builder, String text) {
        builder.append('"');
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '"' -> builder.append("\\\"");
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> appendCharacter(builder, character);
            }
        }
        builder.append('"');
    }

    private static void appendCharacter(StringBuilder builder, char character) {
        if (character < 0x20) {
            builder.append(String.format("\\u%04x", (int) character));
        } else {
            builder.append(character);
        }
    }
}
