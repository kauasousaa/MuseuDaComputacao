package museu.interpreter.basic;

final class IfStatement implements StatementHandler {

    private final ExpressionEvaluator evaluator;
    private final StatementDispatcher dispatcher;

    IfStatement(ExpressionEvaluator evaluator, StatementDispatcher dispatcher) {
        this.evaluator = evaluator;
        this.dispatcher = dispatcher;
    }

    @Override
    public String keyword() {
        return "IF";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        boolean condition = BasicValues.isTrue(evaluator.evaluate(cursor, context));
        if (cursor.matchKeyword("GOTO")) {
            jumpIfTrue(condition, cursor, context);
            return;
        }
        cursor.expectKeyword("THEN");
        Token next = cursor.peek();
        if (next != null && next.type() == TokenType.NUMBER) {
            jumpIfTrue(condition, cursor, context);
        } else if (condition) {
            dispatcher.dispatch(cursor, context);
        }
    }

    private static void jumpIfTrue(boolean condition, TokenCursor cursor, ExecutionContext context) {
        int targetLine = cursor.expectLineNumber();
        cursor.expectEnd();
        if (condition) {
            context.jumpToLine(targetLine);
        }
    }
}
