package museu.memory;

import museu.era.Era;
import museu.shared.exception.ValidationException;
import museu.shared.text.TextNormalizer;

public final class MemoryValidator {

    public static final int MIN_TEXT_LENGTH = 3;
    public static final int MAX_TEXT_LENGTH = 280;
    public static final int MAX_AUTHOR_LENGTH = 40;
    public static final int MAX_QUESTION_LENGTH = 200;

    public MemoryDraft validate(NewMemoryRequest request) {
        String text = TextNormalizer.clean(request.text());
        if (text.length() < MIN_TEXT_LENGTH) {
            throw new ValidationException("Conte a sua memória com pelo menos " + MIN_TEXT_LENGTH + " caracteres.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new ValidationException("A memória pode ter no máximo " + MAX_TEXT_LENGTH + " caracteres.");
        }
        if (request.eraCode() == null || request.eraCode().isBlank()) {
            throw new ValidationException("Informe a época aproximada da memória.");
        }
        Era era = Era.parse(request.eraCode());
        String authorName = optionalField(request.authorName(), MAX_AUTHOR_LENGTH, "O nome ou apelido");
        String guideQuestion = optionalField(request.guideQuestion(), MAX_QUESTION_LENGTH, "A pergunta-guia");
        return new MemoryDraft(text, era, authorName, guideQuestion);
    }

    private static String optionalField(String value, int maxLength, String fieldDescription) {
        String cleaned = TextNormalizer.clean(value).replace('\n', ' ');
        if (cleaned.length() > maxLength) {
            throw new ValidationException(fieldDescription + " pode ter no máximo " + maxLength + " caracteres.");
        }
        return cleaned.isEmpty() ? null : cleaned;
    }
}
