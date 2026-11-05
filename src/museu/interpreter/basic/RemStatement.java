package museu.interpreter.basic;

final class RemStatement implements StatementHandler {

    @Override
    public String keyword() {
        return "REM";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        // Comentários não têm efeito: o tokenizador já descartou o restante da linha.
    }
}
