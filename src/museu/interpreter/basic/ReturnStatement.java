package museu.interpreter.basic;

final class ReturnStatement implements StatementHandler {

    @Override
    public String keyword() {
        return "RETURN";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        cursor.expectEnd();
        context.jumpToIndex(context.popReturnIndex());
    }
}
