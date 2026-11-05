package museu.interpreter.basic;

final class LetStatement implements StatementHandler {

    private final ExpressionEvaluator evaluator;

    LetStatement(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String keyword() {
        return "LET";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        String variableName = cursor.expectIdentifier();
        cursor.expectOperator("=");
        Object value = evaluator.evaluate(cursor, context);
        cursor.expectEnd();
        context.setVariable(variableName, value);
    }
}
