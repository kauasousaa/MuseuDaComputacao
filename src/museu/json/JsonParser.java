package museu.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {

    private static final int MAX_DEPTH = 64;

    private final String source;
    private int position;
    private int depth;

    private JsonParser(String source) {
        this.source = source;
    }

    public static Object parse(String source) {
        if (source == null || source.isBlank()) {
            throw new JsonParseException("O corpo da requisição deve conter um JSON.");
        }
        JsonParser parser = new JsonParser(source);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.isAtEnd()) {
            throw parser.error("conteúdo inesperado após o fim do JSON");
        }
        return value;
    }

    private Object readValue() {
        if (isAtEnd()) {
            throw error("fim inesperado do JSON");
        }
        return switch (source.charAt(position)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        enterNesting();
        Map<String, Object> result = new LinkedHashMap<>();
        position++;
        skipWhitespace();
        if (tryConsume('}')) {
            exitNesting();
            return result;
        }
        do {
            skipWhitespace();
            if (peek() != '"') {
                throw error("era esperada uma chave entre aspas");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            result.put(key, readValue());
            skipWhitespace();
        } while (tryConsume(','));
        expect('}');
        exitNesting();
        return result;
    }

    private List<Object> readArray() {
        enterNesting();
        List<Object> result = new ArrayList<>();
        position++;
        skipWhitespace();
        if (tryConsume(']')) {
            exitNesting();
            return result;
        }
        do {
            skipWhitespace();
            result.add(readValue());
            skipWhitespace();
        } while (tryConsume(','));
        expect(']');
        exitNesting();
        return result;
    }

    private String readString() {
        position++;
        StringBuilder builder = new StringBuilder();
        while (!isAtEnd()) {
            char character = source.charAt(position++);
            if (character == '"') {
                return builder.toString();
            }
            if (character == '\\') {
                builder.append(readEscapedCharacter());
            } else {
                builder.append(character);
            }
        }
        throw error("texto sem aspas de fechamento");
    }

    private char readEscapedCharacter() {
        if (isAtEnd()) {
            throw error("escape incompleto");
        }
        char escaped = source.charAt(position++);
        return switch (escaped) {
            case '"', '\\', '/' -> escaped;
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> readUnicodeEscape();
            default -> throw error("escape inválido \\" + escaped);
        };
    }

    private char readUnicodeEscape() {
        if (position + 4 > source.length()) {
            throw error("escape unicode incompleto");
        }
        String hex = source.substring(position, position + 4);
        position += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException exception) {
            throw error("escape unicode inválido");
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!source.startsWith(literal, position)) {
            throw error("valor inválido");
        }
        position += literal.length();
        return value;
    }

    private Number readNumber() {
        int start = position;
        while (!isAtEnd() && "+-0123456789.eE".indexOf(source.charAt(position)) >= 0) {
            position++;
        }
        String text = source.substring(start, position);
        if (text.isEmpty()) {
            throw error("valor inválido");
        }
        try {
            boolean isDecimal = text.contains(".") || text.contains("e") || text.contains("E");
            return isDecimal ? Double.parseDouble(text) : Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw error("número inválido: " + text);
        }
    }

    private void enterNesting() {
        if (++depth > MAX_DEPTH) {
            throw error("JSON aninhado demais");
        }
    }

    private void exitNesting() {
        depth--;
    }

    private void expect(char expected) {
        if (!tryConsume(expected)) {
            throw error("era esperado '" + expected + "'");
        }
    }

    private boolean tryConsume(char expected) {
        if (!isAtEnd() && source.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(position);
    }

    private void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(source.charAt(position))) {
            position++;
        }
    }

    private boolean isAtEnd() {
        return position >= source.length();
    }

    private JsonParseException error(String detail) {
        return new JsonParseException("JSON inválido na posição " + position + ": " + detail + ".");
    }
}
