package museu.interpreter.basic;

final class ForStatement implements StatementHandler {

    private final ExpressionEvaluator evaluator;

    ForStatement(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String keyword() {
        return "FOR";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        String variableName = cursor.expectIdentifier();
        cursor.expectOperator("=");
        double start = BasicValues.asNumber(evaluator.evaluate(cursor, context));
        cursor.expectKeyword("TO");
        double limit = BasicValues.asNumber(evaluator.evaluate(cursor, context));
        double step = cursor.matchKeyword("STEP") ? BasicValues.asNumber(evaluator.evaluate(cursor, context)) : 1;
        cursor.expectEnd();
        if (step == 0) {
            throw new BasicException("STEP NAO PODE SER ZERO");
        }
        context.setVariable(variableName, start);
        context.forLoops().removeIf(frame -> frame.variableName().equals(variableName));
        context.forLoops().push(new ForLoopFrame(variableName, limit, step, context.nextIndex()));
    }
}
