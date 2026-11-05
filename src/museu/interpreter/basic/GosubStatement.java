package museu.interpreter.basic;

final class GosubStatement implements StatementHandler {

    @Override
    public String keyword() {
        return "GOSUB";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        int targetLine = cursor.expectLineNumber();
        cursor.expectEnd();
        context.pushReturnIndex(context.nextIndex());
        context.jumpToLine(targetLine);
    }
}
