package museu.interpreter.basic;

import java.util.List;

@FunctionalInterface
interface BasicFunction {

    Object apply(List<Object> arguments, ExecutionContext context);
}
