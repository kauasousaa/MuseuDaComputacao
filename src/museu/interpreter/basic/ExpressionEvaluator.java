package museu.interpreter.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class ExpressionEvaluator {

    private static final Set<String> COMPARISON_OPERATORS = Set.of("=", "<>", "<", ">", "<=", ">=");

    private final BasicFunctionRegistry functions;

    ExpressionEvaluator(BasicFunctionRegistry functions) {
        this.functions = functions;
    }

    Object evaluate(TokenCursor cursor, ExecutionContext context) {
        return parseOr(cursor, context);
    }

    private Object parseOr(TokenCursor cursor, ExecutionContext context) {
        Object left = parseAnd(cursor, context);
        while (cursor.matchKeyword("OR")) {
            Object right = parseAnd(cursor, context);
            left = BasicValues.fromBoolean(BasicValues.isTrue(left) || BasicValues.isTrue(right));
        }
        return left;
    }

    private Object parseAnd(TokenCursor cursor, ExecutionContext context) {
        Object left = parseNot(cursor, context);
        while (cursor.matchKeyword("AND")) {
            Object right = parseNot(cursor, context);
            left = BasicValues.fromBoolean(BasicValues.isTrue(left) && BasicValues.isTrue(right));
        }
        return left;
    }

    private Object parseNot(TokenCursor cursor, ExecutionContext context) {
        if (cursor.matchKeyword("NOT")) {
            return BasicValues.fromBoolean(!BasicValues.isTrue(parseNot(cursor, context)));
        }
        return parseComparison(cursor, context);
    }

    private Object parseComparison(TokenCursor cursor, ExecutionContext context) {
        Object left = parseAdditive(cursor, context);
        Token next = cursor.peek();
        if (next == null || next.type() != TokenType.OPERATOR || !COMPARISON_OPERATORS.contains(next.text())) {
            return left;
        }
        cursor.next();
        Object right = parseAdditive(cursor, context);
        return BasicValues.fromBoolean(compare(left, next.text(), right));
    }

    private static boolean compare(Object left, String operator, Object right) {
        int comparison = left instanceof String
                ? BasicValues.asString(left).compareTo(BasicValues.asString(right))
                : Double.compare(BasicValues.asNumber(left), BasicValues.asNumber(right));
        return switch (operator) {
            case "=" -> comparison == 0;
            case "<>" -> comparison != 0;
            case "<" -> comparison < 0;
            case ">" -> comparison > 0;
            case "<=" -> comparison <= 0;
            default -> comparison >= 0;
        };
    }

    private Object parseAdditive(TokenCursor cursor, ExecutionContext context) {
        Object left = parseTerm(cursor, context);
        while (true) {
            if (cursor.matchOperator("+")) {
                left = add(left, parseTerm(cursor, context));
            } else if (cursor.matchOperator("-")) {
                left = BasicValues.asNumber(left) - BasicValues.asNumber(parseTerm(cursor, context));
            } else {
                return left;
            }
        }
    }

    private static Object add(Object left, Object right) {
        if (left instanceof String) {
            return BasicValues.requireMaxLength(BasicValues.asString(left) + BasicValues.asString(right));
        }
        return BasicValues.requireFinite(BasicValues.asNumber(left) + BasicValues.asNumber(right));
    }

    private Object parseTerm(TokenCursor cursor, ExecutionContext context) {
        Object left = parsePower(cursor, context);
        while (true) {
            if (cursor.matchOperator("*")) {
                left = BasicValues.requireFinite(BasicValues.asNumber(left) * BasicValues.asNumber(parsePower(cursor, context)));
            } else if (cursor.matchOperator("/")) {
                left = divide(BasicValues.asNumber(left), BasicValues.asNumber(parsePower(cursor, context)));
            } else if (cursor.matchKeyword("MOD")) {
                left = modulo(BasicValues.asNumber(left), BasicValues.asNumber(parsePower(cursor, context)));
            } else {
                return left;
            }
        }
    }

    private static double divide(double dividend, double divisor) {
        if (divisor == 0) {
            throw new BasicException("DIVISAO POR ZERO");
        }
        return dividend / divisor;
    }

    private static double modulo(double dividend, double divisor) {
        if (divisor == 0) {
            throw new BasicException("DIVISAO POR ZERO");
        }
        return dividend % divisor;
    }

    private Object parsePower(TokenCursor cursor, ExecutionContext context) {
        Object base = parseUnary(cursor, context);
        if (cursor.matchOperator("^")) {
            double exponent = BasicValues.asNumber(parsePower(cursor, context));
            return BasicValues.requireFinite(Math.pow(BasicValues.asNumber(base), exponent));
        }
        return base;
    }

    private Object parseUnary(TokenCursor cursor, ExecutionContext context) {
        if (cursor.matchOperator("-")) {
            return -BasicValues.asNumber(parseUnary(cursor, context));
        }
        if (cursor.matchOperator("+")) {
            return BasicValues.asNumber(parseUnary(cursor, context));
        }
        return parsePrimary(cursor, context);
    }

    private Object parsePrimary(TokenCursor cursor, ExecutionContext context) {
        Token token = cursor.next();
        return switch (token.type()) {
            case NUMBER -> parseNumber(token.text());
            case STRING -> token.text();
            case IDENTIFIER -> functions.contains(token.text())
                    ? parseFunctionCall(token.text(), cursor, context)
                    : context.getVariable(token.text());
            case OPERATOR -> parseParenthesized(token, cursor, context);
        };
    }

    private static double parseNumber(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException exception) {
            throw new BasicException("NUMERO INVALIDO " + text);
        }
    }

    private Object parseParenthesized(Token token, TokenCursor cursor, ExecutionContext context) {
        if (!token.isOperator("(")) {
            throw new BasicException("ERRO DE SINTAXE PERTO DE '" + token.text() + "'");
        }
        cursor.enterNesting();
        Object value = evaluate(cursor, context);
        cursor.expectOperator(")");
        cursor.exitNesting();
        return value;
    }

    private Object parseFunctionCall(String name, TokenCursor cursor, ExecutionContext context) {
        cursor.expectOperator("(");
        cursor.enterNesting();
        List<Object> arguments = new ArrayList<>();
        if (!cursor.matchOperator(")")) {
            do {
                arguments.add(evaluate(cursor, context));
            } while (cursor.matchOperator(","));
            cursor.expectOperator(")");
        }
        cursor.exitNesting();
        return functions.call(name, arguments, context);
    }
}
