package museu.interpreter.basic;

import java.util.Deque;

final class NextStatement implements StatementHandler {

    @Override
    public String keyword() {
        return "NEXT";
    }

    @Override
    public void execute(TokenCursor cursor, ExecutionContext context) {
        Deque<ForLoopFrame> loops = context.forLoops();
        String variableName = cursor.isAtEnd() ? null : cursor.expectIdentifier();
        cursor.expectEnd();
        if (variableName != null) {
            discardInnerLoops(loops, variableName);
        }
        if (loops.isEmpty()) {
            throw new BasicException("NEXT SEM FOR");
        }
        ForLoopFrame frame = loops.peek();
        double nextValue = BasicValues.asNumber(context.getVariable(frame.variableName())) + frame.step();
        context.setVariable(frame.variableName(), nextValue);
        if (frame.shouldContinue(nextValue)) {
            context.jumpToIndex(frame.bodyStartIndex());
        } else {
            loops.pop();
        }
    }

    private static void discardInnerLoops(Deque<ForLoopFrame> loops, String variableName) {
        while (!loops.isEmpty() && !loops.peek().variableName().equals(variableName)) {
            loops.pop();
        }
    }
}
