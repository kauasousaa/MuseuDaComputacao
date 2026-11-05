package museu.ai;

import java.util.Map;
import museu.ai.eliza.ElizaChatbot;
import museu.http.Controller;
import museu.http.HttpRequest;
import museu.http.HttpResponse;
import museu.http.Router;

public final class AiController implements Controller {

    private final ElizaChatbot elizaChatbot;

    public AiController(ElizaChatbot elizaChatbot) {
        this.elizaChatbot = elizaChatbot;
    }

    @Override
    public void registerRoutes(Router router) {
        router.post("/api/ai/eliza", this::talkToEliza);
    }

    private HttpResponse talkToEliza(HttpRequest request) {
        String message = request.jsonBody().getStringOrDefault("message", "");
        return HttpResponse.ok(Map.of("reply", elizaChatbot.reply(message)));
    }
}
