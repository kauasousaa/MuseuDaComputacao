package museu.interpreter.basic;

record Token(TokenType type, String text) {

    boolean isOperator(String operator) {
        return type == TokenType.OPERATOR && text.equals(operator);
    }

    boolean isKeyword(String keyword) {
        return type == TokenType.IDENTIFIER && text.equals(keyword);
    }
}
