package museu.interpreter.basic;

record ForLoopFrame(String variableName, double limit, double step, int bodyStartIndex) {

    boolean shouldContinue(double currentValue) {
        return step > 0 ? currentValue <= limit : currentValue >= limit;
    }
}
