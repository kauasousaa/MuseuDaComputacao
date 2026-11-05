package museu.interpreter.basic;

import java.util.HashMap;
import java.util.Map;

final class StatementDispatcher {

    private final Map<String, StatementHandler> handlers = new HashMap<>();
    private final StatementHandler implicitAssignment;

    StatementDispatcher(StatementHandler implicitAssignment) {
        this.implicitAssignment = implicitAssignment;
    }

    void register(StatementHandler handler) {
        handlers.put(handler.keyword(), handler);
    }

    void dispatch(TokenCursor cursor, ExecutionContext context) {
        Token first = cursor.peek();
        if (first == null) {
            return;
        }
        if (first.type() != TokenType.IDENTIFIER) {
            throw new BasicException("ERRO DE SINTAXE PERTO DE '" + first.text() + "'");
        }
        StatementHandler handler = handlers.get(first.text());
        if (handler == null) {
            implicitAssignment.execute(cursor, context);
            return;
        }
        cursor.next();
        handler.execute(cursor, context);
    }
}
