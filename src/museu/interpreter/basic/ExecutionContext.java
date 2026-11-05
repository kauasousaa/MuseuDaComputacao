package museu.interpreter.basic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

final class ExecutionContext {

    private static final int MAX_OUTPUT_LENGTH = 20_000;
    private static final int MAX_STEPS = 50_000;
    private static final int MAX_GOSUB_DEPTH = 200;

    private final BasicProgram program;
    private final Random random;
    private final Map<String, Object> variables = new HashMap<>();
    private final Deque<ForLoopFrame> forLoops = new ArrayDeque<>();
    private final Deque<Integer> returnIndexes = new ArrayDeque<>();
    private final StringBuilder output = new StringBuilder();
    private int nextIndex;
    private int steps;
    private boolean halted;

    ExecutionContext(BasicProgram program, Random random) {
        this.program = program;
        this.random = random;
    }

    boolean hasNextLine() {
        return !halted && nextIndex < program.size();
    }

    BasicLine advance() {
        if (++steps > MAX_STEPS) {
            throw new BasicException("LIMITE DE " + MAX_STEPS + " PASSOS EXCEDIDO (LOOP INFINITO?)");
        }
        BasicLine line = program.lineAt(nextIndex);
        nextIndex++;
        return line;
    }

    int nextIndex() {
        return nextIndex;
    }

    void jumpToLine(int lineNumber) {
        nextIndex = program.indexOf(lineNumber);
    }

    void jumpToIndex(int index) {
        nextIndex = index;
    }

    void halt() {
        halted = true;
    }

    Object getVariable(String name) {
        return variables.getOrDefault(name, BasicValues.defaultValueFor(name));
    }

    void setVariable(String name, Object value) {
        BasicValues.requireCompatible(name, value);
        variables.put(name, value);
    }

    Deque<ForLoopFrame> forLoops() {
        return forLoops;
    }

    void pushReturnIndex(int index) {
        if (returnIndexes.size() >= MAX_GOSUB_DEPTH) {
            throw new BasicException("PILHA DE GOSUB ESTOUROU");
        }
        returnIndexes.push(index);
    }

    int popReturnIndex() {
        if (returnIndexes.isEmpty()) {
            throw new BasicException("RETURN SEM GOSUB");
        }
        return returnIndexes.pop();
    }

    void print(String text) {
        if (output.length() + text.length() > MAX_OUTPUT_LENGTH) {
            throw new BasicException("LIMITE DE SAIDA EXCEDIDO");
        }
        output.append(text);
    }

    double nextRandom() {
        return random.nextDouble();
    }

    String output() {
        return output.toString();
    }

    int steps() {
        return steps;
    }
}
