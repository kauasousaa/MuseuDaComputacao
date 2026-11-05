package museu.interpreter.basic;

public record BasicExecutionResult(String output, int steps, String error) {

    static BasicExecutionResult success(String output, int steps) {
        return new BasicExecutionResult(output, steps, null);
    }

    static BasicExecutionResult failure(String output, int steps, String error) {
        return new BasicExecutionResult(output, steps, error);
    }
}
