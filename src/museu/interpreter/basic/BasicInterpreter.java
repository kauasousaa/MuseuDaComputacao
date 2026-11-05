package museu.interpreter.basic;

import java.util.Random;
import java.util.function.Supplier;
import museu.shared.exception.ValidationException;

public final class BasicInterpreter {

    public static final int MAX_SOURCE_LENGTH = 10_000;

    private final BasicProgramParser parser = new BasicProgramParser(new BasicTokenizer());
    private final StatementDispatcher dispatcher;
    private final Supplier<Random> randomFactory;

    public BasicInterpreter(Supplier<Random> randomFactory) {
        this.randomFactory = randomFactory;
        ExpressionEvaluator evaluator = new ExpressionEvaluator(BasicFunctionRegistry.withDefaults());
        LetStatement letStatement = new LetStatement(evaluator);
        this.dispatcher = new StatementDispatcher(letStatement);
        dispatcher.register(letStatement);
        dispatcher.register(new PrintStatement(evaluator));
        dispatcher.register(new IfStatement(evaluator, dispatcher));
        dispatcher.register(new ForStatement(evaluator));
        dispatcher.register(new NextStatement());
        dispatcher.register(new GotoStatement());
        dispatcher.register(new GosubStatement());
        dispatcher.register(new ReturnStatement());
        dispatcher.register(new RemStatement());
        dispatcher.register(new EndStatement("END"));
        dispatcher.register(new EndStatement("STOP"));
    }

    public BasicExecutionResult run(String source) {
        validateSource(source);
        ExecutionContext context = null;
        try {
            context = new ExecutionContext(parser.parse(source), randomFactory.get());
            execute(context);
            return BasicExecutionResult.success(context.output(), context.steps());
        } catch (BasicException exception) {
            String partialOutput = context == null ? "" : context.output();
            int steps = context == null ? 0 : context.steps();
            return BasicExecutionResult.failure(partialOutput, steps, exception.formattedMessage());
        }
    }

    private void execute(ExecutionContext context) {
        while (context.hasNextLine()) {
            BasicLine line = context.advance();
            try {
                dispatcher.dispatch(new TokenCursor(line.tokens()), context);
            } catch (BasicException exception) {
                throw exception.atLine(line.number());
            }
        }
    }

    private static void validateSource(String source) {
        if (source == null || source.isBlank()) {
            throw new ValidationException("Escreva um programa BASIC para executar.");
        }
        if (source.length() > MAX_SOURCE_LENGTH) {
            throw new ValidationException("O programa pode ter no máximo " + MAX_SOURCE_LENGTH + " caracteres.");
        }
    }
}
