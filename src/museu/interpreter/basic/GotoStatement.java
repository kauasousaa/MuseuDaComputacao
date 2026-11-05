package museu.interpreter.basic;

final class GotoStatement implements StatementHandler {

    @Override
    public String keyword() {
        return "GOTO";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        int targetLine = cursor.expectLineNumber();
        cursor.expectEnd();
        context.jumpToLine(targetLine);
    }
}
