package museu.interpreter.basic;

final class PrintStatement implements StatementHandler {

    private final ExpressionEvaluator evaluator;

    PrintStatement(ExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public String keyword() {
        return "PRINT";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        StringBuilder line = new StringBuilder();
        boolean endsWithNewLine = true;
        while (!cursor.isAtEnd()) {
            if (cursor.matchOperator(";")) {
                endsWithNewLine = false;
            } else if (cursor.matchOperator(",")) {
                line.append('\t');
                endsWithNewLine = false;
            } else {
                line.append(BasicValues.format(evaluator.evaluate(cursor, context)));
                endsWithNewLine = true;
            }
        }
        context.print(endsWithNewLine ? line.append('\n').toString() : line.toString());
    }
}
