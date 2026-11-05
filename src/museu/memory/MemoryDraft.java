package museu.memory;

import museu.era.Era;

public record MemoryDraft(String text, Era era, String authorName, String guideQuestion) {
}
