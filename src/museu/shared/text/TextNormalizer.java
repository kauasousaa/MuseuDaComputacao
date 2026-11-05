package museu.shared.text;

import java.util.regex.Pattern;

public final class TextNormalizer {

    private static final Pattern CONTROL_CHARACTERS = Pattern.compile("[\\p{Cntrl}&&[^\\n]]");
    private static final Pattern EXCESSIVE_LINE_BREAKS = Pattern.compile("\\n{3,}");
    private static final Pattern REPEATED_SPACES = Pattern.compile("[ \\t]{2,}");

    private TextNormalizer() {
    }

    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        String withoutControls = CONTROL_CHARACTERS.matcher(text.replace("\r\n", "\n")).replaceAll("");
        String compactSpaces = REPEATED_SPACES.matcher(withoutControls).replaceAll(" ");
        return EXCESSIVE_LINE_BREAKS.matcher(compactSpaces).replaceAll("\n\n").strip();
    }
}
