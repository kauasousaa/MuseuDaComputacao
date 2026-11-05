package museu.interpreter.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class BasicTokenizer {

    private static final String COMMENT_KEYWORD = "REM";
    private static final Set<String> TWO_CHARACTER_OPERATORS = Set.of("<=", ">=", "<>");
    private static final String SINGLE_CHARACTER_OPERATORS = "+-*/^=<>(),;:";

    List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        while (position < text.length()) {
            char current = text.charAt(position);
            if (Character.isWhitespace(current)) {
                position++;
            } else if (startsNumber(text, position)) {
                position = readNumber(text, position, tokens);
            } else if (current == '"') {
                position = readString(text, position, tokens);
            } else if (Character.isLetter(current)) {
                position = readIdentifier(text, position, tokens);
                if (lastTokenIsComment(tokens)) {
                    break;
                }
            } else {
                position = readOperator(text, position, tokens);
            }
        }
        return tokens;
    }

    private static boolean startsNumber(String text, int position) {
        char current = text.charAt(position);
        boolean decimalStart = current == '.' && position + 1 < text.length()
                && Character.isDigit(text.charAt(position + 1));
        return Character.isDigit(current) || decimalStart;
    }

    private static int readNumber(String text, int start, List<Token> tokens) {
        int end = start;
        while (end < text.length() && (Character.isDigit(text.charAt(end)) || text.charAt(end) == '.')) {
            end++;
        }
        tokens.add(new Token(TokenType.NUMBER, text.substring(start, end)));
        return end;
    }

    private static int readString(String text, int start, List<Token> tokens) {
        int closingQuote = text.indexOf('"', start + 1);
        if (closingQuote < 0) {
            throw new BasicException("TEXTO SEM ASPAS DE FECHAMENTO");
        }
        tokens.add(new Token(TokenType.STRING, text.substring(start + 1, closingQuote)));
        return closingQuote + 1;
    }

    private static int readIdentifier(String text, int start, List<Token> tokens) {
        int end = start;
        while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) {
            end++;
        }
        if (end < text.length() && text.charAt(end) == '$') {
            end++;
        }
        tokens.add(new Token(TokenType.IDENTIFIER, text.substring(start, end).toUpperCase(Locale.ROOT)));
        return end;
    }

    private static int readOperator(String text, int start, List<Token> tokens) {
        String twoCharacters = start + 2 <= text.length() ? text.substring(start, start + 2) : "";
        if (TWO_CHARACTER_OPERATORS.contains(twoCharacters)) {
            tokens.add(new Token(TokenType.OPERATOR, twoCharacters));
            return start + 2;
        }
        char current = text.charAt(start);
        if (SINGLE_CHARACTER_OPERATORS.indexOf(current) < 0) {
            throw new BasicException("CARACTERE INVALIDO '" + current + "'");
        }
        tokens.add(new Token(TokenType.OPERATOR, String.valueOf(current)));
        return start + 1;
    }

    private static boolean lastTokenIsComment(List<Token> tokens) {
        return tokens.get(tokens.size() - 1).isKeyword(COMMENT_KEYWORD);
    }
}
