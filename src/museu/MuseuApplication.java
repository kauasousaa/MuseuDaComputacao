package museu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import museu.ai.AiController;
import museu.ai.eliza.ElizaChatbot;
import museu.ai.eliza.ElizaScript;
import museu.config.ServerConfig;
import museu.content.JsonContentRepository;
import museu.content.ReadOnlyContentController;
import museu.content.model.Comparison;
import museu.content.model.GuideQuestion;
import museu.content.model.ProgrammingLanguage;
import museu.content.model.RecognizeChallenge;
import museu.content.model.TimelineEvent;
import museu.era.EraController;
import museu.era.EraVoteService;
import museu.era.JsonFileEraVoteRepository;
import museu.health.HealthController;
import museu.http.Controller;
import museu.http.ExceptionMapper;
import museu.http.Router;
import museu.http.StaticFileHandler;
import museu.http.WebServer;
import museu.interpreter.InterpreterController;
import museu.interpreter.basic.BasicInterpreter;
import museu.json.JsonFileStore;
import museu.memory.JsonFileMemoryRepository;
import museu.memory.MemoryController;
import museu.memory.MemoryService;
import museu.memory.MemoryValidator;
import museu.shared.security.AdminAuthorizer;
import museu.shared.security.SlidingWindowRateLimiter;
import museu.timeline.TimelineController;
import museu.timeline.TimelineService;

public final class MuseuApplication {

    private static final Logger LOGGER = Logger.getLogger(MuseuApplication.class.getName());
    private static final int MEMORIES_PER_MINUTE = 10;
    private static final int VOTES_PER_MINUTE = 30;

    private MuseuApplication() {
    }

    public static WebServer start(ServerConfig config) throws IOException {
        ensureDirectoryExists(config.webRoot(), "front-end (web)");
        ensureDirectoryExists(config.contentDirectory(), "conteúdo (data/content)");

        Router router = new Router();
        createControllers(config).forEach(controller -> controller.registerRoutes(router));

        WebServer webServer = new WebServer(router, new StaticFileHandler(config.webRoot()), new ExceptionMapper());
        webServer.start(config.port());
        Runtime.getRuntime().addShutdownHook(new Thread(webServer::stop));
        LOGGER.info(() -> "Museu da Computação aberto em http://localhost:" + config.port());
        return webServer;
    }

    private static List<Controller> createControllers(ServerConfig config) {
        Clock clock = Clock.systemUTC();
        Path content = config.contentDirectory();
        Path runtime = config.runtimeDirectory();

        TimelineService timelineService = new TimelineService(
                new JsonContentRepository<>(content.resolve("timeline.json"), TimelineEvent::fromJson));

        MemoryService memoryService = new MemoryService(
                new JsonFileMemoryRepository(new JsonFileStore(runtime.resolve("memories.json"))),
                new MemoryValidator(),
                new SlidingWindowRateLimiter(MEMORIES_PER_MINUTE, Duration.ofMinutes(1), clock),
                new AdminAuthorizer(config.adminKey()),
                clock);

        EraVoteService eraVoteService = new EraVoteService(
                new JsonFileEraVoteRepository(new JsonFileStore(runtime.resolve("eraVotes.json"))),
                new SlidingWindowRateLimiter(VOTES_PER_MINUTE, Duration.ofMinutes(1), clock));

        ElizaChatbot elizaChatbot = new ElizaChatbot(ElizaScript.load(content.resolve("eliza.json")), new Random());

        return List.of(
                new HealthController(clock),
                new TimelineController(timelineService),
                new ReadOnlyContentController<>("/api/languages",
                        new JsonContentRepository<>(content.resolve("languages.json"), ProgrammingLanguage::fromJson)),
                new ReadOnlyContentController<>("/api/comparisons",
                        new JsonContentRepository<>(content.resolve("comparisons.json"), Comparison::fromJson)),
                new ReadOnlyContentController<>("/api/challenges",
                        new JsonContentRepository<>(content.resolve("challenges.json"), RecognizeChallenge::fromJson)),
                new ReadOnlyContentController<>("/api/guide-questions",
                        new JsonContentRepository<>(content.resolve("guideQuestions.json"), GuideQuestion::fromJson)),
                new EraController(eraVoteService),
                new MemoryController(memoryService),
                new InterpreterController(new BasicInterpreter(Random::new)),
                new AiController(elizaChatbot));
    }

    private static void ensureDirectoryExists(Path directory, String description) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalStateException("Pasta de " + description + " não encontrada em " + directory
                    + ". Execute o servidor a partir da raiz do projeto MuseuDaComputacao.");
        }
    }
}
