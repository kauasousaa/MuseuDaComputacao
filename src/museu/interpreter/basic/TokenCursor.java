package museu.interpreter.basic;

import java.util.List;

final class TokenCursor {

    private static final int MAX_NESTING_DEPTH = 64;

    private final List<Token> tokens;
    private int position;
    private int nestingDepth;

    TokenCursor(List<Token> tokens) {
        this.tokens = tokens;
    }

    boolean isAtEnd() {
        return position >= tokens.size();
    }

    Token peek() {
        return isAtEnd() ? null : tokens.get(position);
    }

    Token next() {
        if (isAtEnd()) {
            throw new BasicException("ERRO DE SINTAXE: COMANDO INCOMPLETO");
        }
        return tokens.get(position++);
    }

    boolean matchOperator(String operator) {
        if (!isAtEnd() && peek().isOperator(operator)) {
            position++;
            return true;
        }
        return false;
    }

    boolean matchKeyword(String keyword) {
        if (!isAtEnd() && peek().isKeyword(keyword)) {
            position++;
            return true;
        }
        return false;
    }

    void expectOperator(String operator) {
        if (!matchOperator(operator)) {
            throw new BasicException("ERRO DE SINTAXE: ESPERAVA '" + operator + "'");
        }
    }

    void expectKeyword(String keyword) {
        if (!matchKeyword(keyword)) {
            throw new BasicException("ERRO DE SINTAXE: ESPERAVA " + keyword);
        }
    }

    String expectIdentifier() {
        Token token = next();
        if (token.type() != TokenType.IDENTIFIER) {
            throw new BasicException("ERRO DE SINTAXE: ESPERAVA UMA VARIAVEL");
        }
        return token.text();
    }

    int expectLineNumber() {
        Token token = next();
        if (token.type() != TokenType.NUMBER) {
            throw new BasicException("ERRO DE SINTAXE: ESPERAVA NUMERO DE LINHA");
        }
        try {
            return Integer.parseInt(token.text());
        } catch (NumberFormatException exception) {
            throw new BasicException("NUMERO DE LINHA INVALIDO");
        }
    }

    void expectEnd() {
        if (!isAtEnd()) {
            throw new BasicException("ERRO DE SINTAXE PERTO DE '" + peek().text() + "'");
        }
    }

    void enterNesting() {
        if (++nestingDepth > MAX_NESTING_DEPTH) {
            throw new BasicException("EXPRESSAO COMPLEXA DEMAIS");
        }
    }

    void exitNesting() {
        nestingDepth--;
    }
}
