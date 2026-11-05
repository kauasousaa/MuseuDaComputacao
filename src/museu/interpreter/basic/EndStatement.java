package museu.interpreter.basic;

final class EndStatement implements StatementHandler {

    private final String keyword;

    EndStatement(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String keyword() {
        return keyword;
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        cursor.expectEnd();
        context.halt();
    }
}
