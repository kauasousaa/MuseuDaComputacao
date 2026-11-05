package museu.ai.eliza;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import museu.shared.exception.ValidationException;
import museu.shared.text.TextNormalizer;

public final class ElizaChatbot {

    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");
    private static final String CAPTURE_PLACEHOLDER = "{0}";

    private final ElizaScript script;
    private final Random random;

    public ElizaChatbot(ElizaScript script, Random random) {
        this.script = script;
        this.random = random;
    }

    public String reply(String message) {
        String normalizedMessage = normalize(message);
        for (ElizaRule rule : script.rules()) {
            Matcher matcher = rule.pattern().matcher(normalizedMessage);
            if (matcher.find()) {
                return fillTemplate(pickOne(rule.responses()), matcher);
            }
        }
        return pickOne(script.fallbackResponses());
    }

    private static String normalize(String message) {
        String cleaned = TextNormalizer.clean(message).replace('\n', ' ');
        if (cleaned.isEmpty()) {
            throw new ValidationException("Escreva alguma coisa para a ELIZA.");
        }
        if (cleaned.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("A ELIZA só lê mensagens de até " + MAX_MESSAGE_LENGTH + " caracteres.");
        }
        return cleaned.toLowerCase(PORTUGUESE);
    }

    private String fillTemplate(String template, Matcher matcher) {
        if (!template.contains(CAPTURE_PLACEHOLDER) || matcher.groupCount() == 0 || matcher.group(1) == null) {
            return template;
        }
        return template.replace(CAPTURE_PLACEHOLDER, reflect(matcher.group(1)));
    }

    private String reflect(String fragment) {
        String withoutPunctuation = fragment.replaceAll("[.!?,;]+$", "").strip();
        return Arrays.stream(withoutPunctuation.split("\\s+"))
                .map(word -> script.reflections().getOrDefault(word, word))
                .collect(Collectors.joining(" "));
    }

    private String pickOne(List<String> options) {
        return options.get(random.nextInt(options.size()));
    }
}
