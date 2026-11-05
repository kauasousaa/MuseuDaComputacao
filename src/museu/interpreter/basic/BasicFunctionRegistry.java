package museu.interpreter.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class BasicFunctionRegistry {

    private final Map<String, FunctionDefinition> functions = new HashMap<>();

    static BasicFunctionRegistry withDefaults() {
        BasicFunctionRegistry registry = new BasicFunctionRegistry();
        registry.register("ABS", 1, (arguments, context) -> Math.abs(BasicValues.asNumber(arguments.get(0))));
        registry.register("INT", 1, (arguments, context) -> Math.floor(BasicValues.asNumber(arguments.get(0))));
        registry.register("SQR", 1, (arguments, context) -> squareRoot(BasicValues.asNumber(arguments.get(0))));
        registry.register("RND", 1, (arguments, context) -> context.nextRandom());
        registry.register("LEN", 1, (arguments, context) -> (double) BasicValues.asString(arguments.get(0)).length());
        registry.register("STR$", 1, (arguments, context) -> BasicValues.format(BasicValues.asNumber(arguments.get(0))));
        return registry;
    }

    void register(String name, int arity, BasicFunction function) {
        functions.put(name, new FunctionDefinition(arity, function));
    }

    boolean contains(String name) {
        return functions.containsKey(name);
    }

    Object call(String name, List<Object> arguments, ExecutionContext context) {
        FunctionDefinition definition = functions.get(name);
        if (arguments.size() != definition.arity()) {
            throw new BasicException(name + " ESPERA " + definition.arity() + " ARGUMENTO(S)");
        }
        return definition.function().apply(arguments, context);
    }

    private static double squareRoot(double value) {
        if (value < 0) {
            throw new BasicException("RAIZ DE NUMERO NEGATIVO");
        }
        return Math.sqrt(value);
    }

    private record FunctionDefinition(int arity, BasicFunction function) {
    }
}
