package museu.interpreter.basic;

interface StatementHandler {

    String keyword();

    void execute(TokenCursor cursor, ExecutionContext context);
}
