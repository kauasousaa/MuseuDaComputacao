package museu.interpreter;

import museu.http.Controller;
import museu.http.HttpRequest;
import museu.http.HttpResponse;
import museu.http.Router;
import museu.interpreter.basic.BasicInterpreter;

public final class InterpreterController implements Controller {

    private final BasicInterpreter basicInterpreter;

    public InterpreterController(BasicInterpreter basicInterpreter) {
        this.basicInterpreter = basicInterpreter;
    }

    @Override
    public void registerRoutes(Router router) {
        router.post("/api/interpreters/basic", this::runBasic);
    }

    private HttpResponse runBasic(HttpRequest request) {
        String source = request.jsonBody().getStringOrDefault("source", "");
        return HttpResponse.ok(basicInterpreter.run(source));
    }
}
